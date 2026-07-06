#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"

TOKEN=""

require_tool() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required tool: $1" >&2
    exit 1
  fi
}

request_json() {
  local method="$1"
  local path="$2"
  local data="${3:-}"
  local expected="${4:-200}"
  local body_file
  local status
  local curl_args

  body_file="$(mktemp)"
  curl_args=(-sS -o "$body_file" -w "%{http_code}" -X "$method" "$BASE_URL$path")

  if [ -n "$TOKEN" ]; then
    curl_args+=(-H "Authorization: $TOKEN")
  fi

  if [ -n "$data" ]; then
    curl_args+=(-H "Content-Type: application/json" -d "$data")
  fi

  status="$(curl "${curl_args[@]}")"

  if [ "$status" != "$expected" ]; then
    echo "Request failed: $method $path" >&2
    echo "Expected HTTP $expected, got HTTP $status" >&2
    echo "Response body:" >&2
    cat "$body_file" >&2
    rm -f "$body_file"
    exit 1
  fi

  cat "$body_file"
  rm -f "$body_file"
}

login() {
  local response

  response="$(request_json POST /login "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASSWORD\"}" 200)"
  TOKEN="$(echo "$response" | jq -r '.token')"

  if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "Login did not return a token" >&2
    exit 1
  fi
}

get_json() {
  request_json GET "$1" "" 200
}

post_json() {
  request_json POST "$1" "$2" "$3" >/dev/null
}

balance_account_id() {
  local name="$1"
  local id

  id="$(find_balance_account_id "$name")"
  if [ -z "$id" ] || [ "$id" = "null" ]; then
    echo "Balance account not found: $name" >&2
    exit 1
  fi
  echo "$id"
}

account_id() {
  local name="$1"
  local id

  id="$(find_account_id "$name")"
  if [ -z "$id" ] || [ "$id" = "null" ]; then
    echo "Account not found: $name" >&2
    exit 1
  fi
  echo "$id"
}

payment_type_id() {
  local type="$1"
  local id

  id="$(find_payment_type_id "$type")"
  if [ -z "$id" ] || [ "$id" = "null" ]; then
    echo "Payment type not found: $type" >&2
    exit 1
  fi
  echo "$id"
}

product_id() {
  local name="$1"
  local id

  id="$(find_product_id "$name")"
  if [ -z "$id" ] || [ "$id" = "null" ]; then
    echo "Product not found: $name" >&2
    exit 1
  fi
  echo "$id"
}

client_id() {
  local full_name="$1"
  local id

  id="$(find_client_id "$full_name")"
  if [ -z "$id" ] || [ "$id" = "null" ]; then
    echo "Client not found: $full_name" >&2
    exit 1
  fi
  echo "$id"
}

find_balance_account_id() {
  local name="$1"

  get_json /accounts/balance | jq -r --arg name "$name" '.[] | select(.name == $name) | .id' | head -n 1
}

find_account_id() {
  local name="$1"

  get_json /accounts | jq -r --arg name "$name" '.[] | select(.name == $name) | .id' | head -n 1
}

find_payment_type_id() {
  local type="$1"

  get_json /payment-types | jq -r --arg type "$type" '.[] | select(.type == $type) | .id' | head -n 1
}

find_product_id() {
  local name="$1"

  get_json /products | jq -r --arg name "$name" '.[] | select(.name == $name) | .id' | head -n 1
}

find_client_id() {
  local full_name="$1"

  get_json /clients | jq -r --arg fullName "$full_name" '.[] | select(.fullName == $fullName) | .id' | head -n 1
}

entry_exists() {
  local description="$1"
  local body_file
  local status

  body_file="$(mktemp)"
  status="$(curl -sS -o "$body_file" -w "%{http_code}" -H "Authorization: $TOKEN" "$BASE_URL/journal")"
  if [ "$status" = "404" ]; then
    rm -f "$body_file"
    return 1
  fi
  if [ "$status" != "200" ]; then
    echo "Request failed: GET /journal" >&2
    echo "Expected HTTP 200 or 404, got HTTP $status" >&2
    echo "Response body:" >&2
    cat "$body_file" >&2
    rm -f "$body_file"
    exit 1
  fi

  if jq -e --arg description "$description" '.[] | select(.description == $description)' "$body_file" >/dev/null; then
    rm -f "$body_file"
    return 0
  fi

  rm -f "$body_file"
  return 1
}

create_payment_account() {
  local name="$1"
  local parent_id="$2"
  local id

  id="$(find_balance_account_id "$name")"
  if [ -n "$id" ] && [ "$id" != "null" ]; then
    echo "Payment account already exists: $name"
    return
  fi

  post_json "/accounts/balance?id=$parent_id" "{\"name\":\"$name\"}" 201
}

create_payment_type() {
  local type="$1"
  local account_id="$2"
  local id

  id="$(find_payment_type_id "$type")"
  if [ -n "$id" ] && [ "$id" != "null" ]; then
    echo "Payment type already exists: $type"
    return
  fi

  post_json /payment-types/create "{\"type\":\"$type\",\"accountId\":$account_id}" 201
}

create_initial_funding_entry() {
  local caja_id="$1"
  local banco_id="$2"
  local tarjeta_id="$3"
  local capital_id="$4"
  local description="Aporte inicial de capital para medios de pago mock"

  if entry_exists "$description"; then
    echo "Funding entry already exists."
    return
  fi

  post_json /entry/create "{
    \"description\":\"$description\",
    \"movements\":[
      {\"account\":$caja_id,\"debit\":\"150000\",\"credit\":\"0\"},
      {\"account\":$banco_id,\"debit\":\"130000\",\"credit\":\"0\"},
      {\"account\":$tarjeta_id,\"debit\":\"80000\",\"credit\":\"0\"},
      {\"account\":$capital_id,\"debit\":\"0\",\"credit\":\"360000\"}
    ]
  }" 201
}

create_client() {
  local full_name="$1"
  local email="$2"
  local cuit="$3"
  local vat_condition="$4"
  local document_type="$5"
  local document_number="$6"
  local address="$7"
  local id

  id="$(find_client_id "$full_name")"
  if [ -n "$id" ] && [ "$id" != "null" ]; then
    echo "Client already exists: $full_name"
    return
  fi

  post_json /clients/create "{
    \"fullName\":\"$full_name\",
    \"email\":\"$email\",
    \"cuit\":\"$cuit\",
    \"vatCondition\":\"$vat_condition\",
    \"documentType\":\"$document_type\",
    \"documentNumber\":\"$document_number\",
    \"commercialAddress\":\"$address\"
  }" 201
}

create_product() {
  local name="$1"
  local sale_price="$2"
  local unit_price="$3"
  local stock="$4"
  local payment_type_id="$5"
  local amount="$6"
  local id

  id="$(find_product_id "$name")"
  if [ -n "$id" ] && [ "$id" != "null" ]; then
    echo "Product already exists: $name"
    return
  fi

  post_json /products/create "{
    \"name\":\"$name\",
    \"salePrice\":$sale_price,
    \"lot\":{\"unitPrice\":$unit_price,\"stock\":$stock},
    \"payments\":[{\"id\":$payment_type_id,\"amount\":$amount}]
  }" 201
}

create_sale() {
  local client_id="$1"
  local payment_method="$2"
  local installments="$3"
  local discount="$4"
  local invoice_type="$5"
  local amount="$6"
  local items="$7"

  post_json /sales "{
    \"clientId\":$client_id,
    \"items\":$items,
    \"payments\":[{\"method\":\"$payment_method\",\"amount\":$amount}],
    \"installments\":$installments,
    \"discount\":$discount,
    \"invoiceType\":\"$invoice_type\"
  }" 201
}

main() {
  require_tool curl
  require_tool jq

  echo "Logging in at $BASE_URL..."
  login

  echo "Creating payment accounts..."
  local caja_y_banco_id
  caja_y_banco_id="$(account_id "Caja y Banco")"
  create_payment_account "Banco Mock" "$caja_y_banco_id"
  create_payment_account "Tarjeta Mock" "$caja_y_banco_id"

  local caja_id
  local banco_id
  local tarjeta_id
  local capital_id
  caja_id="$(balance_account_id "Caja")"
  banco_id="$(balance_account_id "Banco Mock")"
  tarjeta_id="$(balance_account_id "Tarjeta Mock")"
  capital_id="$(balance_account_id "Capital")"

  echo "Creating payment types..."
  create_payment_type "Efectivo" "$caja_id"
  create_payment_type "Transferencia" "$banco_id"
  create_payment_type "Tarjeta" "$tarjeta_id"

  local efectivo_id
  local transferencia_id
  local tarjeta_payment_id
  efectivo_id="$(payment_type_id "Efectivo")"
  transferencia_id="$(payment_type_id "Transferencia")"
  tarjeta_payment_id="$(payment_type_id "Tarjeta")"

  echo "Funding payment accounts from Capital..."
  create_initial_funding_entry "$caja_id" "$banco_id" "$tarjeta_id" "$capital_id"

  echo "Creating clients..."
  create_client "Ana Rodriguez" "ana.rodriguez@example.com" "20301234561" "CONSUMIDOR_FINAL" "DNI" "30123456" "Av. Siempre Viva 123"
  create_client "Mercado Norte SRL" "compras@mercadonorte.test" "30711222334" "IVA_RESPONSABLE_INSCRIPTO" "CUIT" "30711222334" "San Martin 450"
  create_client "Carlos Perez" "carlos.perez@example.com" "20224567891" "RESPONSABLE_MONOTRIBUTO" "CUIL" "20224567891" "Belgrano 820"

  echo "Creating products and initial purchase lots..."
  create_product "Yerba mate" 2200 1200 40 "$efectivo_id" 48000
  create_product "Cafe molido" 4200 2500 25 "$transferencia_id" 62500
  create_product "Azucar comun" 1600 900 50 "$efectivo_id" 45000
  create_product "Aceite girasol" 3100 1800 30 "$tarjeta_payment_id" 54000
  create_product "Galletitas surtidas" 1300 700 60 "$transferencia_id" 42000

  local ana_id
  local mercado_id
  local carlos_id
  local yerba_id
  local cafe_id
  local azucar_id
  local aceite_id
  local galletitas_id
  ana_id="$(client_id "Ana Rodriguez")"
  mercado_id="$(client_id "Mercado Norte SRL")"
  carlos_id="$(client_id "Carlos Perez")"
  yerba_id="$(product_id "Yerba mate")"
  cafe_id="$(product_id "Cafe molido")"
  azucar_id="$(product_id "Azucar comun")"
  aceite_id="$(product_id "Aceite girasol")"
  galletitas_id="$(product_id "Galletitas surtidas")"

  echo "Creating sales..."
  create_sale "$ana_id" "Efectivo" 1 0 "B" 15000 "[{\"productId\":$yerba_id,\"quantity\":3},{\"productId\":$cafe_id,\"quantity\":2}]"
  create_sale "$mercado_id" "Transferencia" 1 5 "A" 13490 "[{\"productId\":$azucar_id,\"quantity\":5},{\"productId\":$aceite_id,\"quantity\":2}]"
  create_sale "$carlos_id" "Tarjeta" 3 0 "C" 7800 "[{\"productId\":$galletitas_id,\"quantity\":6}]"
  create_sale "$ana_id" "Efectivo" 1 10 "B" 14580 "[{\"productId\":$cafe_id,\"quantity\":1},{\"productId\":$yerba_id,\"quantity\":4},{\"productId\":$azucar_id,\"quantity\":2}]"
  create_sale "$mercado_id" "Transferencia" 1 0 "A" 9300 "[{\"productId\":$aceite_id,\"quantity\":3}]"
  create_sale "$carlos_id" "Tarjeta" 6 0 "C" 17400 "[{\"productId\":$galletitas_id,\"quantity\":10},{\"productId\":$yerba_id,\"quantity\":2}]"

  echo "Mock data loaded."
  echo "Clients: 3 | Products: 5 | Sales: 6 | Payment types: 3"
}

main "$@"
