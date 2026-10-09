/** CPF / CNPJ check-digit validation — same algorithm as customer-service, for instant feedback in the form. */

const allSame = (d: string) => new Set(d).size === 1;
const digits = (value: string) => value.replace(/\D/g, "");

export const isValidCpf = (value: string) => {
  const d = digits(value);
  if (d.length !== 11 || allSame(d)) return false;
  const check = (length: number) => {
    let sum = 0;
    for (let i = 0; i < length; i++) sum += Number(d[i]) * (length + 1 - i);
    const rest = (sum * 10) % 11;
    return rest === 10 ? 0 : rest;
  };
  return check(9) === Number(d[9]) && check(10) === Number(d[10]);
};

export const isValidCnpj = (value: string) => {
  const d = digits(value);
  if (d.length !== 14 || allSame(d)) return false;
  const check = (weights: number[]) => {
    const sum = weights.reduce((s, w, i) => s + Number(d[i]) * w, 0);
    const rest = sum % 11;
    return rest < 2 ? 0 : 11 - rest;
  };
  return (
    check([5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) === Number(d[12]) &&
    check([6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]) === Number(d[13])
  );
};
