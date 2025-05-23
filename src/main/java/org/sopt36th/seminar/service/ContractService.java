package org.sopt36th.seminar.service;


import java.util.List;
import lombok.RequiredArgsConstructor;

import org.sopt36th.seminar.domain.Contract;
import org.sopt36th.seminar.domain.PreferentialRate;
import org.sopt36th.seminar.dto.response.AccountResponse;
import org.sopt36th.seminar.dto.response.GetAccountRatesResponse;
import org.sopt36th.seminar.dto.response.GetAllAccountsResponse;
import org.sopt36th.seminar.common.exception.custom.ContractNotFoundException;
import org.sopt36th.seminar.domain.Deposit;
import org.sopt36th.seminar.domain.Saving;
import org.sopt36th.seminar.dto.response.GetContractDetailResponse;
import org.sopt36th.seminar.dto.response.GetContractStateResponse;
import org.sopt36th.seminar.mapper.ContractMapper;
import org.sopt36th.seminar.mapper.ContractStateMapper;
import org.sopt36th.seminar.repository.ContractRepository;
import org.sopt36th.seminar.repository.DepositRepository;
import org.sopt36th.seminar.repository.PreferentialRateRepository;
import org.springframework.stereotype.Service;

import static org.sopt36th.seminar.common.exception.GlobalErrorCode.CONTRACT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final PreferentialRateRepository preferentialRateRepository;
    private final ContractRepository contractRepository;
    private final DepositRepository depositRepository;

    public GetAllAccountsResponse getAllAccounts() {
        List<Contract> contracts = contractRepository.findAll();

        int totalAccountBalance = contracts.stream()
                .mapToInt(Contract::getTotalBalance)
                .sum();
        List<AccountResponse> accountResponses = ContractMapper.toAccountResponseList(contracts);

        return new GetAllAccountsResponse(totalAccountBalance, accountResponses);
    }

    public GetContractDetailResponse getContractDetail(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(CONTRACT_NOT_FOUND));
        Saving saving = contract.getSaving();

        double totalPreferentialRate = preferentialRateRepository.sumAllRates(contract.getId());
        List<Deposit> deposits = depositRepository.findByContractId(contract.getId());

        return ContractMapper.toGetContractDetail(contract, saving, deposits, totalPreferentialRate);
    }

    public GetContractStateResponse getContractState(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(CONTRACT_NOT_FOUND));
        Deposit deposit = depositRepository.findTopByContractIdOrderByCreatedAtDesc(contractId);

        return ContractStateMapper.toGetContractState(contract, deposit);
    }

    public GetAccountRatesResponse getAccountRates(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(CONTRACT_NOT_FOUND));
        List<PreferentialRate> preferentialRates = preferentialRateRepository.findAllByContractId(contract.getId());

        return ContractMapper.toAccountRatesResponse(contract, preferentialRates);
    }
}
