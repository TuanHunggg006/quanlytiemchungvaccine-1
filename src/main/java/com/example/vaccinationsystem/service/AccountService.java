package com.example.vaccinationsystem.service;

import com.example.vaccinationsystem.dao.AccountDao;
import com.example.vaccinationsystem.dto.AccountCreateRequest;
import com.example.vaccinationsystem.dto.AccountInfoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public List<AccountInfoDTO> getAllAccounts() {
        return accountDao.findAllWithDetails();
    }

    @Transactional
    public String createAccount(AccountCreateRequest request) {
        String username = clean(request.getUsername());
        String password = clean(request.getPassword());
        String email = clean(request.getEmail());
        String name = clean(request.getName());
        String role = normalizeAuthority(request.getAuthority());

        validateCreateRequest(username, password, name, role);

        if (accountDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        String newAcctId = accountDao.getNextAccountId();

        RoleMeta meta = getRoleMeta(role);
        String newEmpId = accountDao.getNextEmployeeId(meta.table(), meta.idColumn(), meta.idPrefix());

        accountDao.insertAccount(newAcctId, username, password, email, role);
        accountDao.insertEmployeeRow(meta.table(), meta.idColumn(), newEmpId, newAcctId, name, meta.roleLabel());

        return newAcctId;
    }

    @Transactional
    public void updateAccount(String id, AccountCreateRequest request) {
        List<AccountInfoDTO> all = accountDao.findAllWithDetails();
        AccountInfoDTO current = all.stream()
                .filter(a -> a.getAccountId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));

        String oldRole = normalizeAuthority(current.getAuthority());
        String newRole = normalizeAuthority(request.getAuthority());
        String newName = clean(request.getName());

        accountDao.updateAccount(id, clean(request.getEmail()), clean(request.getPassword()));

        if (!newRole.equals(oldRole)) {
            String oldTable = getTableName(oldRole);

            if (!oldTable.isEmpty() && isRoleUsed(oldTable, id)) {
                throw new RuntimeException("Không thể đổi vai trò vì nhân sự này đã có dữ liệu liên quan");
            }

            if (!oldTable.isEmpty()) {
                accountDao.deleteFromRoleTable(oldTable, id);
            }

            RoleMeta meta = getRoleMeta(newRole);
            String newEmpId = accountDao.getNextEmployeeId(meta.table(), meta.idColumn(), meta.idPrefix());

            accountDao.insertEmployeeRow(meta.table(), meta.idColumn(), newEmpId, id, newName, meta.roleLabel());
            accountDao.updateAuthority(id, newRole);
        } else {
            String table = getTableName(oldRole);
            if (!table.isEmpty()) {
                accountDao.updateEmployeeName(table, id, newName);
            }
        }
    }

    private boolean isRoleUsed(String table, String accountId) {
        if (table.equals("DOCTOR")) {
            Optional<String> docId = accountDao.findDoctorIdByAccountId(accountId);
            return docId.isPresent() && accountDao.isDoctorUsed(docId.get());
        }
        if (table.equals("CASHIER")) {
            Optional<String> cashId = accountDao.findCashierIdByAccountId(accountId);
            return cashId.isPresent() && accountDao.isCashierUsed(cashId.get());
        }
        if (table.equals("INVENTORY_MANAGER")) {
            Optional<String> invId = accountDao.findInventoryManagerIdByAccountId(accountId);
            return invId.isPresent() && accountDao.isInventoryManagerUsed(invId.get());
        }
        return false;
    }

    private void validateCreateRequest(String username, String password, String name, String role) {
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password không được để trống");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên nhân viên không được để trống");
        }
        if (role.isEmpty()) {
            throw new IllegalArgumentException("Vai trò không được để trống");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeAuthority(String authority) {
        if (authority == null) return "";
        String value = authority.trim().toUpperCase(Locale.ROOT);

        return switch (value) {
            case "ADMIN" -> "ADMINISTRATOR";
            case "IM" -> "INVENTORY_MANAGER";
            default -> value;
        };
    }

    private String getTableName(String role) {
        return switch (normalizeAuthority(role)) {
            case "DOCTOR" -> "DOCTOR";
            case "CASHIER" -> "CASHIER";
            case "INVENTORY_MANAGER" -> "INVENTORY_MANAGER";
            case "ADMINISTRATOR" -> "ADMINISTRATOR";
            default -> "";
        };
    }

    private RoleMeta getRoleMeta(String role) {
        return switch (normalizeAuthority(role)) {
            case "DOCTOR" -> new RoleMeta("DOCTOR", "DOCTOR_ID", "DOC", "Vaccination Doctor");
            case "CASHIER" -> new RoleMeta("CASHIER", "CASHIER_ID", "CAS", "Cashier");
            case "INVENTORY_MANAGER" -> new RoleMeta("INVENTORY_MANAGER", "INVENTORY_MANAGER_ID", "IM", "Inventory Manager");
            case "ADMINISTRATOR" -> new RoleMeta("ADMINISTRATOR", "ADMINISTRATOR_ID", "AD", "System Admin");
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }

    @Transactional
    public void deleteAccount(String id) {
        accountDao.deleteAccountCascade(id);
    }

    public List<AccountInfoDTO> searchStaff(String role, String keyword) {
        return accountDao.searchStaff(role, keyword);
    }

    private record RoleMeta(String table, String idColumn, String idPrefix, String roleLabel) {}
}
