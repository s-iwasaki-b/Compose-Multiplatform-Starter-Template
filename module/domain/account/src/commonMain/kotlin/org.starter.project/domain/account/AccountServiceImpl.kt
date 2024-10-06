package org.starter.project.domain.account

import androidx.annotation.OpenForTesting
import org.starter.project.data.repository.AccountRepository
import org.starter.project.domain.service.AccountService
import org.starter.project.domain.service.ResultHandler

@OpenForTesting
open class AccountServiceImpl(
    private val resultHandler: ResultHandler,
    private val accountRepository: AccountRepository
) : AccountService {

}
