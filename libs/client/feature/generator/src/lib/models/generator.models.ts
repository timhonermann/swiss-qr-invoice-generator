export type Creditor = {
  iban: string;
  name: string;
  streetName: string;
  streetNumber: string;
  postalCode: string;
  city: string;
  country: 'CH';
  phone: string;
  email: string;
  logoBase64: string | null;
};

export type UltimateDebtor = Omit<
  Creditor,
  'iban' | 'phone' | 'email' | 'logoBase64'
>;

export type Item = {
  description: string;
  quantity: number;
  vat: number;
  unitPrice: number;
};

export type Invoice = {
  title: string;
  invoiceDate: Date;
  dueDate: Date;
  periodFrom: Date;
  periodTo: Date;
  vatNumber: string;
  reference: string;
  creditor: Creditor;
  ultimateDebtor: UltimateDebtor;
  items: Item[];
};
