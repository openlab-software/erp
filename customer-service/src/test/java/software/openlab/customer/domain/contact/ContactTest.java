package software.openlab.customer.domain.contact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import software.openlab.customer.domain.shared.BadRequestException;

class ContactTest {

    @Test
    void trimsAndDropsBlanksAndKeepsTheIdBeingEdited() {
        ContactId id = ContactId.generate();

        Contact contact = Contact.validated(id, new Contact(null, "  Ana Souza ", " Compradora ", " ana@acme.com ",
                "   ", null, true));

        assertEquals(id, contact.contactId());
        assertEquals("Ana Souza", contact.name());
        assertEquals("Compradora", contact.jobTitle());
        assertEquals("ana@acme.com", contact.email());
        assertNull(contact.phone());
    }

    @Test
    void aNewContactGetsAnId() {
        Contact contact = Contact.validated(null, new Contact(null, "Ana", null, null, null, null, false));
        assertEquals(contact.contactId().toString().substring(0, 8), "contact_");
    }

    @Test
    void rejectsBlankNameAndBadEmail() {
        assertThrows(BadRequestException.class,
                () -> Contact.validated(null, new Contact(null, "  ", null, null, null, null, false)));
        assertThrows(BadRequestException.class,
                () -> Contact.validated(null, new Contact(null, "Ana", null, "ana@", null, null, false)));
    }
}
