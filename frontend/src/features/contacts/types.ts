// customer-service serializes snake_case; the BFF passes it through untouched.
/** A contact person of a customer; a customer has up to 20 and at most one has `is_primary`. */
export interface Contact {
  contact_id: string;
  name: string;
  job_title: string | null;
  email: string | null;
  phone: string | null;
  notes: string | null;
  is_primary: boolean;
}

export interface ContactList {
  data: Contact[];
}
