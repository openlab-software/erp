/** "05/10/2026 18:11" in the user's local time zone; "—" for missing values. */
export const formatDateTime = (iso: string | null | undefined) => {
  if (!iso) return "—";
  const date = new Date(iso);
  const day = date.toLocaleDateString("pt-BR");
  const time = date.toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });
  return `${day} ${time}`;
};

/** "123.456.789-09" / "12.345.678/0001-95"; anything that is not 11 or 14 digits is returned as is. */
export const formatDocument = (document: string) => {
  const d = document.replace(/\D/g, "");
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
  if (d.length === 14)
    return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
  return document;
};
