package com.sistema_contable.sistema.contable.services.accounting;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistema_contable.sistema.contable.exceptions.accounting.EntryNotFindException;
import com.sistema_contable.sistema.contable.model.accounting.Movement;
import com.sistema_contable.sistema.contable.repository.MovementRepository;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.LedgerService;

@Service
public class LedgerServiceImp implements LedgerService {
    //dependencies
    @Autowired
    private MovementRepository  movementRepository;

    public List<Movement> LadgerByAccountBetweem(Long accountID, Date before, Date after)throws Exception{
        List<Movement> movements = movementRepository.ledgerAccountBetween(accountID, before, after);
        if(movements.isEmpty()){throw new EntryNotFindException("ERROR : Not found entrys by account between dates");}
        return movements;
    }
}
