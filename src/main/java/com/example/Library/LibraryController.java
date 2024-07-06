package com.example.Library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/library")
public class LibraryController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private LoanRepository loanRepository;

    @PostMapping("/addBook")
    public String addBook(@RequestBody Book book) {
        bookRepository.save(book);
        return "Book added successfully";
    }

    @DeleteMapping("/deleteBook/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "Book deleted successfully";
    }

    @PostMapping("/addReader")
    public String addReader(@RequestBody Reader reader) {
        readerRepository.save(reader);
        return "Reader added successfully";
    }

    @DeleteMapping("/deleteReader/{id}")
    public String deleteReader(@PathVariable Long id) {
        readerRepository.deleteById(id);
        return "Reader deleted successfully";
    }

    @GetMapping("/findBook")
    public List<Book> findBooks(@RequestParam String query) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().contains(query)
                        || book.getAuthor().contains(query)
                        || book.getPublisher().contains(query))
                .toList();
    }

    @GetMapping("/findReader")
    public List<Reader> findReaders(@RequestParam String query) {
        return readerRepository.findAll().stream()
                .filter(reader -> reader.getFirstName().contains(query)
                        || reader.getLastName().contains(query)
                        || reader.getPhoneNumber().contains(query))
                .toList();
    }

    @PostMapping("/borrowBook")
    public String borrowBook(@RequestParam Long readerId, @RequestParam Long bookId) {
        var reader = readerRepository.findById(readerId).orElse(null);
        var book = bookRepository.findById(bookId).orElse(null);

        if (reader == null || book == null) {
            return "Reader or book not found";
        }

        if (book.getNumberOfCopies() - book.getNumberOfCopiesBorrowed() <= 0) {
            return "No copies available";
        }

        var loan = new Loan();
        loan.setReader(reader);
        loan.setBook(book);
        loan.setBorrowDate(LocalDate.now());
        loan.setReturned(false);
        loanRepository.save(loan);

        book.setNumberOfCopiesBorrowed(book.getNumberOfCopiesBorrowed() + 1);
        bookRepository.save(book);

        reader.getBorrowedBooks().add(book);
        readerRepository.save(reader);

        return "Book borrowed successfully";
    }

    @PostMapping("/returnBook")
    public String returnBook(@RequestParam Long loanId) {
        var loan = loanRepository.findById(loanId).orElse(null);

        if (loan == null || loan.isReturned()) {
            return "Loan not found or book already returned";
        }

        loan.setReturned(true);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);

        var book = loan.getBook();
        book.setNumberOfCopiesBorrowed(book.getNumberOfCopiesBorrowed() - 1);
        bookRepository.save(book);

        var reader = loan.getReader();
        reader.getBorrowedBooks().remove(book);
        readerRepository.save(reader);

        return "Book returned successfully";
    }

    @GetMapping("/findLoansByReader/{readerId}")
    public List<Loan> findLoansByReader(@PathVariable Long readerId) {
        var reader = readerRepository.findById(readerId).orElse(null);

        if (reader == null) {
            return List.of();
        }

        return loanRepository.findAll().stream()
                .filter(loan -> loan.getReader().getId().equals(readerId))
                .toList();
    }
}