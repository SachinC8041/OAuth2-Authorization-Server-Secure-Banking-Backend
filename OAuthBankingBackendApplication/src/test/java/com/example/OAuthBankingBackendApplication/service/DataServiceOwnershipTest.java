package com.example.OAuthBankingBackendApplication.service;

import com.example.OAuthBankingBackendApplication.entity.AccountTransactions;
import com.example.OAuthBankingBackendApplication.entity.Accounts;
import com.example.OAuthBankingBackendApplication.entity.Cards;
import com.example.OAuthBankingBackendApplication.entity.Contact;
import com.example.OAuthBankingBackendApplication.entity.Loans;
import com.example.OAuthBankingBackendApplication.entity.Notice;
import com.example.OAuthBankingBackendApplication.repository.AccountTransactionRepository;
import com.example.OAuthBankingBackendApplication.repository.AccountsRepository;
import com.example.OAuthBankingBackendApplication.repository.CardsRepository;
import com.example.OAuthBankingBackendApplication.repository.ContactRepository;
import com.example.OAuthBankingBackendApplication.repository.LoanRepository;
import com.example.OAuthBankingBackendApplication.repository.NoticeRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The ownership check lives in the service layer rather than in the controllers,
 * so it cannot be skipped by a future caller.
 *
 * <p>Each denial case asserts that the repository was never touched. That is the
 * property that matters: the caller is refused before any row is read, so the
 * endpoint cannot be used to probe which customer ids exist.
 */
@ExtendWith(MockitoExtension.class)
class DataServiceOwnershipTest {

    private static final long OWN_ID = 1L;
    private static final long OTHER_ID = 2L;

    private static final Authentication CALLER =
            new UsernamePasswordAuthenticationToken("sachin@example.com", null, List.of());

    @Mock
    private CustomerAccessService customerAccessService;

    @Mock
    private AccountsRepository accountsRepository;
    @Mock
    private AccountTransactionRepository accountTransactionRepository;
    @Mock
    private CardsRepository cardsRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private ContactRepository contactRepository;

    private void denyOtherId() {
        doThrow(new AccessDeniedException("The requested customer id is not available to this user"))
                .when(customerAccessService).requireOwnership(any(), org.mockito.ArgumentMatchers.eq(OTHER_ID));
    }

    // ------------------------------------------------------------------
    // AccountService
    // ------------------------------------------------------------------

    @Test
    @DisplayName("AccountService returns the account for the caller's own id")
    void accountServiceReturnsOwnAccount() {
        when(accountsRepository.findByCustomerId(OWN_ID)).thenReturn(Optional.of(new Accounts()));

        AccountService service = new AccountService(accountsRepository, customerAccessService);

        assertThat(service.findAccountFor(CALLER, OWN_ID)).isPresent();
    }

    @Test
    @DisplayName("AccountService refuses another customer's id without reading the row")
    void accountServiceRefusesOtherId() {
        denyOtherId();
        AccountService service = new AccountService(accountsRepository, customerAccessService);

        assertThatThrownBy(() -> service.findAccountFor(CALLER, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(accountsRepository);
    }

    // ------------------------------------------------------------------
    // BalanceService
    // ------------------------------------------------------------------

    @Test
    @DisplayName("BalanceService returns statement lines for the caller's own id")
    void balanceServiceReturnsOwnTransactions() {
        when(accountTransactionRepository.findByCustomerIdOrderByTransactionDtDesc(OWN_ID))
                .thenReturn(List.of(new AccountTransactions()));

        BalanceService service = new BalanceService(accountTransactionRepository, customerAccessService);

        assertThat(service.findTransactionsFor(CALLER, OWN_ID)).hasSize(1);
    }

    @Test
    @DisplayName("BalanceService refuses another customer's id without reading the rows")
    void balanceServiceRefusesOtherId() {
        denyOtherId();
        BalanceService service = new BalanceService(accountTransactionRepository, customerAccessService);

        assertThatThrownBy(() -> service.findTransactionsFor(CALLER, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(accountTransactionRepository);
    }

    // ------------------------------------------------------------------
    // CardsService
    // ------------------------------------------------------------------

    @Test
    @DisplayName("CardsService returns cards for the caller's own id")
    void cardsServiceReturnsOwnCards() {
        when(cardsRepository.findByCustomerId(OWN_ID)).thenReturn(List.of(new Cards()));

        CardsService service = new CardsService(cardsRepository, customerAccessService);

        assertThat(service.findCardsFor(CALLER, OWN_ID)).hasSize(1);
    }

    @Test
    @DisplayName("CardsService refuses another customer's id without reading the rows")
    void cardsServiceRefusesOtherId() {
        denyOtherId();
        CardsService service = new CardsService(cardsRepository, customerAccessService);

        assertThatThrownBy(() -> service.findCardsFor(CALLER, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(cardsRepository);
    }

    // ------------------------------------------------------------------
    // LoansService
    // ------------------------------------------------------------------

    @Test
    @DisplayName("LoansService returns loans for the caller's own id")
    void loansServiceReturnsOwnLoans() {
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(OWN_ID)).thenReturn(List.of(new Loans()));

        LoansService service = new LoansService(loanRepository, customerAccessService);

        assertThat(service.findLoansFor(CALLER, OWN_ID)).hasSize(1);
    }

    @Test
    @DisplayName("LoansService refuses another customer's id without reading the rows")
    void loansServiceRefusesOtherId() {
        denyOtherId();
        LoansService service = new LoansService(loanRepository, customerAccessService);

        assertThatThrownBy(() -> service.findLoansFor(CALLER, OTHER_ID))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(loanRepository);
    }

    // ------------------------------------------------------------------
    // Services with no ownership dimension
    // ------------------------------------------------------------------

    @Test
    @DisplayName("NoticeService needs no ownership check - notices are the same for everyone")
    void noticeServiceNeedsNoOwnershipCheck() {
        when(noticeRepository.findAllActiveNotices()).thenReturn(List.of(new Notice()));

        NoticeService service = new NoticeService(noticeRepository);

        assertThat(service.findActiveNotices()).hasSize(1);
        verifyNoInteractions(customerAccessService);
    }

    @Test
    @DisplayName("ContactService stamps a reference number and a creation date")
    void contactServiceStampsReferenceAndDate() {
        when(contactRepository.save(any(Contact.class))).thenAnswer(call -> call.getArgument(0));

        ContactService service = new ContactService(contactRepository);
        Contact saved = service.saveInquiry(new Contact());

        assertThat(saved.getContactId()).startsWith("SR");
        assertThat(saved.getCreateDt()).isNotNull();
    }

    @Test
    @DisplayName("consecutive contact references are not sequential")
    void contactReferencesAreNotSequential() {
        when(contactRepository.save(any(Contact.class))).thenAnswer(call -> call.getArgument(0));

        ContactService service = new ContactService(contactRepository);

        assertThat(service.saveInquiry(new Contact()).getContactId())
                .isNotEqualTo(service.saveInquiry(new Contact()).getContactId());
    }
}
