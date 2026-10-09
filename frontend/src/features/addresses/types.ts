// customer-service serializes snake_case; the BFF passes it through untouched.
/** A customer has up to 10 addresses and at most one has `is_default`. */
export interface Address {
  address_id: string;
  label: string | null;
  zip_code: string | null;
  street: string | null;
  number: string | null;
  complement: string | null;
  neighborhood: string | null;
  city: string | null;
  state: string | null;
  is_default: boolean;
}

export interface AddressList {
  data: Address[];
}
