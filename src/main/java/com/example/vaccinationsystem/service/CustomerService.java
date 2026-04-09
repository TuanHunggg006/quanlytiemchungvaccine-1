package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.CustomerDao;
import com.example.vaccinationsystem.dto.CustomerDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<CustomerDTO> getAll() {
        return customerDao.findAll();
    }

    public List<CustomerDTO> searchByName(String name) {
        return customerDao.searchByName(name);
    }

    public String createCustomer(CustomerDTO dto) {
        String lastId = customerDao.getLatestCustomerId();
        String nextId = generateNextId(lastId, "CUS");
        customerDao.insertCustomer(nextId, dto);
        return nextId;
    }

    private String generateNextId(String currentId, String prefix) {
        StringBuilder numStr = new StringBuilder();
        for (int i = currentId.length() - 1; i >= 0; i--) {
            char c = currentId.charAt(i);
            if (Character.isDigit(c)) numStr.insert(0, c);
            else break;
        }
        int nextNum = 1;
        if (numStr.length() > 0) {
            nextNum = Integer.parseInt(numStr.toString()) + 1;
        }
        return String.format("%s%03d", prefix, nextNum);
    }
}
