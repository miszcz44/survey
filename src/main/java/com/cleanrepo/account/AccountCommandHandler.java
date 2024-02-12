package com.cleanrepo.account;

import com.cleanrepo.account.command.HostRegisterCommand;
import com.cleanrepo.account.command.RegisterCommand;
import com.cleanrepo.account.command.SetPasswordCommand;
import com.cleanrepo.account.command.UserRegisterCommand;
import com.cleanrepo.account.exception.EmailTakenException;
import com.cleanrepo.account.value_object.EncodedPassword;
import com.cleanrepo.auth.exception.InvalidJwtException;
import com.cleanrepo.auth.exception.UserEmailNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class AccountCommandHandler {

    private final AccountRepository accountRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    Account handleRegisterAdmin(RegisterCommand registerCommand) {
        Optional<Account> alreadyRegisteredAccount = accountRepository.findByEmail_Email(registerCommand.getEmail().getEmail());
        if(alreadyRegisteredAccount.isPresent()){
            log.warn("Unsuccessfully registered - email must be not taken");
            throw new EmailTakenException();
        }
        String encodedPassword = bCryptPasswordEncoder.encode(registerCommand.getPassword().getRawPassword());
        Account newAccount = new Account(registerCommand, encodedPassword);
        log.info("Successfully registered");
        return accountRepository.save(newAccount);
    }
    Account handleRegisterUser(UserRegisterCommand registerCommand) {
        Optional<Account> alreadyRegisteredAccount = accountRepository.findByEmail_Email(registerCommand.getEmail().getEmail());
        if(alreadyRegisteredAccount.isPresent()){
            log.warn("Unsuccessfully registered - email must be not taken");
            throw new EmailTakenException();
        }
        Account newAccount = new Account(registerCommand);
        log.info("Successfully registered");
        return accountRepository.save(newAccount);
    }

    Account handleRegisterHost(HostRegisterCommand registerCommand) {
        Optional<Account> alreadyRegisteredAccount = accountRepository.findByEmail_Email(registerCommand.getEmail().getEmail());
        if(alreadyRegisteredAccount.isPresent()) {
            log.warn("Unsuccessfully registered - email must be not taken");
            throw new EmailTakenException();
        }
        Account newAccount = new Account(registerCommand);
        log.info("Successfully registered");
        return accountRepository.save(newAccount);
    }

    Account handleSetPassword(SetPasswordCommand setPasswordCommand) {
        Optional<Account> account = accountRepository.findByEmail_Email(setPasswordCommand.getEmail().getEmail());
        if(account.isEmpty()){
            log.warn("No user is associated with this email");
            throw new UserEmailNotFoundException();
        }
        else if(!setPasswordCommand.getToken().equals(account.get().getConfirmationToken())) {
            log.warn("Token for setting password is incorrect");
            throw new InvalidJwtException();
        }
        String encodedPassword = bCryptPasswordEncoder.encode(setPasswordCommand.getPassword().getRawPassword());
        account.get().setEncodedPassword(new EncodedPassword(encodedPassword));
        account.get().setConfirmationToken(null);
        return accountRepository.save(account.get());
    }
}
