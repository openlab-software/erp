import styled from "@xstyled/emotion";

export const Page = styled.div`
  padding: 24px 28px 40px;
  & > * {
    animation: fadeUp 280ms cubic-bezier(0.2, 0.8, 0.2, 1) backwards;
  }
  & > *:nth-child(2) {
    animation-delay: 40ms;
  }
  & > *:nth-child(3) {
    animation-delay: 80ms;
  }
  & > *:nth-child(4) {
    animation-delay: 120ms;
  }
`;

export const PageHead = styled.div`
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
  h1 {
    margin: 0 0 4px;
    font-size: 24px;
    font-weight: 600;
    letter-spacing: -0.01em;
  }
`;

export const PageActions = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

export const SectionLabel = styled.div`
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--ink-3);
  margin: 0 0 8px;
`;

export const Subtitle = styled.div`
  font-size: 13px;
  color: var(--ink-3);
`;

export const Card = styled.div`
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  overflow: hidden;
`;

export const CardHead = styled.div`
  padding: 14px 18px;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  h3 {
    margin: 0;
    font-size: 13px;
    font-weight: 600;
    letter-spacing: -0.005em;
  }
`;

export const CardHeadSub = styled.div`
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: 2px;
`;

export const CardBody = styled.div`
  padding: 18px;
`;

export const StatGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  @media (max-width: 1100px) {
    grid-template-columns: repeat(2, 1fr);
    & > *:nth-child(3) {
      border-left: 0;
      border-top: 1px solid var(--line);
    }
    & > *:nth-child(4) {
      border-top: 1px solid var(--line);
    }
  }
`;

export const Stat = styled.div`
  display: flex;
  flex-direction: column;
  padding: 16px 18px;
  & + & {
    border-left: 1px solid var(--line);
  }
`;

export const StatLabel = styled.div`
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--ink-3);
  margin-bottom: 10px;
`;

export const StatValue = styled.div`
  font-size: 24px;
  font-weight: 500;
  letter-spacing: -0.02em;
  color: var(--ink-1);
  font-feature-settings:
    "tnum" 1,
    "zero" 1;
  .unit {
    font-size: 14px;
    color: var(--ink-3);
    margin-left: 4px;
    font-weight: 400;
  }
`;

export const StatDelta = styled.div<{ $pos?: boolean; $neg?: boolean }>`
  font-family: var(--font-mono);
  font-size: 11px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 5px;
  color: ${({ $pos, $neg }) =>
    $pos ? "var(--pos)" : $neg ? "var(--neg)" : "var(--ink-3)"};
`;

export const TableWrap = styled.div`
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  overflow: hidden;
`;

export const TableToolbar = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--line);
  background: var(--surface-2);
`;

export const SearchWrap = styled.div`
  position: relative;
  display: flex;
  align-items: center;
  & > svg {
    position: absolute;
    left: 9px;
    color: var(--ink-3);
    pointer-events: none;
  }
`;

export const FInput = styled.input`
  height: 30px;
  padding: 0 10px 0 30px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
  font-size: 13px;
  color: var(--ink-1);
  outline: none;
  &::placeholder {
    color: var(--ink-4);
  }
  &:focus {
    border-color: var(--accent);
  }
`;

export const FInputStandalone = styled.input`
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--ink-1);
  font-size: 13px;
  outline: none;
  width: 100%;
  transition:
    border-color 100ms,
    box-shadow 100ms;
  &:focus {
    border-color: var(--accent);
    box-shadow: 0 0 0 3px rgba(15, 110, 86, 0.1);
  }
`;

export const FSelect = styled.select`
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--ink-1);
  font-size: 13px;
  outline: none;
  width: 100%;
  transition:
    border-color 100ms,
    box-shadow 100ms;
  &:focus {
    border-color: var(--accent);
    box-shadow: 0 0 0 3px rgba(15, 110, 86, 0.1);
  }
`;

export const FTextarea = styled.textarea`
  padding: 8px 10px;
  border: 1px solid var(--line-strong);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--ink-1);
  font-size: 13px;
  outline: none;
  width: 100%;
  resize: vertical;
  min-height: 80px;
  transition:
    border-color 100ms,
    box-shadow 100ms;
  &:focus {
    border-color: var(--accent);
    box-shadow: 0 0 0 3px rgba(15, 110, 86, 0.1);
  }
`;

export const Btn = styled.button`
  height: 32px;
  padding: 0 14px;
  border-radius: var(--radius);
  border: 1px solid var(--line-strong);
  background: var(--surface);
  color: var(--ink-1);
  font-size: 13px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  transition:
    background 100ms,
    border-color 100ms;
  &:hover {
    background: var(--surface-sunken);
    border-color: var(--ink-4);
  }
  &:active {
    background: var(--line-soft);
  }
`;

export const BtnPrimary = styled(Btn)`
  background: var(--ink-1);
  border-color: var(--ink-1);
  color: #fafafa;
  &:hover {
    background: #2c2a27;
    border-color: #2c2a27;
  }
`;

export const BtnGhost = styled(Btn)`
  background: transparent;
  border-color: transparent;
  color: var(--ink-2);
  &:hover {
    background: var(--surface-sunken);
  }
`;

export const BtnSm = styled(Btn)`
  height: 26px;
  padding: 0 10px;
  font-size: 12px;
`;

export const BtnSmGhost = styled(BtnSm)`
  background: transparent;
  border-color: transparent;
  color: var(--ink-2);
  &:hover {
    background: var(--surface-sunken);
  }
`;

export const BtnIcon = styled(Btn)`
  width: 30px;
  padding: 0;
  justify-content: center;
`;

export const BtnSmGhostIcon = styled(BtnSmGhost)`
  width: 26px;
  padding: 0;
  justify-content: center;
`;

export const T = styled.table`
  width: 100%;
  border-collapse: collapse;
  font-size: var(--fs-body);
  th {
    text-align: left;
    font-weight: 500;
    font-size: var(--fs-small);
    color: var(--ink-3);
    padding: 10px var(--pad-x);
    border-bottom: 1px solid var(--line);
    background: var(--surface-2);
    white-space: nowrap;
    position: sticky;
    top: 0;
    &.num {
      text-align: right;
    }
    &.ck {
      width: 28px;
    }
  }
  td {
    padding: var(--pad-y) var(--pad-x);
    border-bottom: 1px solid var(--line-soft);
    vertical-align: middle;
    height: var(--row-h);
    &.num {
      font-family: var(--font-mono);
      font-feature-settings: "tnum" 1;
      text-align: right;
      white-space: nowrap;
    }
    &.id {
      font-family: var(--font-mono);
      color: var(--ink-3);
      font-size: 12px;
    }
    &.ck {
      width: 28px;
    }
    &.empty {
      text-align: center;
      color: var(--ink-3);
      padding: 40px;
    }
  }
  tbody tr {
    transition: background 80ms;
  }
  tbody tr:hover {
    background: var(--surface-2);
  }
  tbody tr.selected {
    background: var(--surface-sunken);
  }
  input[type="checkbox"] {
    appearance: none;
    width: 14px;
    height: 14px;
    border: 1px solid var(--line-strong);
    border-radius: 3px;
    background: var(--surface);
    display: grid;
    place-items: center;
    cursor: pointer;
    margin: 0;
    &:checked {
      background: var(--ink-1);
      border-color: var(--ink-1);
    }
    &:checked::after {
      content: "";
      width: 7px;
      height: 4px;
      border-left: 1.5px solid #fff;
      border-bottom: 1.5px solid #fff;
      transform: rotate(-45deg) translate(0px, -1px);
    }
  }
  .row-actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 80ms;
  }
  tbody tr:hover .row-actions {
    opacity: 1;
  }
`;

export const Status = styled.span<{
  variant?: "pos" | "neg" | "warn" | "info";
}>`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  font-weight: 500;
  padding: 2px 8px 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
  background: ${({ variant: variant }) => {
    if (variant === "pos") return "var(--pos-soft)";
    if (variant === "neg") return "var(--neg-soft)";
    if (variant === "warn") return "var(--warn-soft)";
    if (variant === "info") return "var(--accent-soft)";
    return "var(--surface-sunken)";
  }};
  color: ${({ variant: variant }) => {
    if (variant === "pos") return "var(--pos)";
    if (variant === "neg") return "var(--neg)";
    if (variant === "warn") return "var(--warn)";
    if (variant === "info") return "var(--accent-ink)";
    return "var(--ink-2)";
  }};
  border: 1px solid
    ${({ variant }) => (variant ? "transparent" : "var(--line)")};
  &::before {
    content: "";
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: ${({ variant }) => {
      if (variant === "pos") return "var(--pos)";
      if (variant === "neg") return "var(--neg)";
      if (variant === "warn") return "var(--warn)";
      if (variant === "info") return "var(--accent)";
      return "var(--ink-3)";
    }};
  }
`;

export const Field = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
  label {
    font-size: 11.5px;
    color: var(--ink-2);
    font-weight: 500;
  }
`;

export const FieldHint = styled.span`
  font-size: 11px;
  color: var(--ink-4);
`;

export const BarTrack = styled.div`
  position: relative;
  height: 6px;
  background: var(--surface-sunken);
  border-radius: 3px;
  overflow: hidden;
`;

export const BarFill = styled.div<{ variant?: "pos" | "neg" | "warn" }>`
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  background: ${({ variant }) =>
    variant === "pos"
      ? "var(--accent)"
      : variant === "neg"
        ? "var(--neg)"
        : variant === "warn"
          ? "var(--warn)"
          : "var(--ink-2)"};
  border-radius: 3px;
`;

export const Kv = styled.dl`
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 10px 16px;
  font-size: 13px;
  dt {
    color: var(--ink-3);
    font-weight: 400;
  }
  dd {
    margin: 0;
    color: var(--ink-1);
  }
`;

export const Tabs = styled.div`
  display: flex;
  border-bottom: 1px solid var(--line);
  margin-bottom: 20px;
`;

export const Tab = styled.button<{ $active?: boolean }>`
  padding: 10px 14px;
  font-size: 13px;
  color: ${({ $active }) => ($active ? "var(--ink-1)" : "var(--ink-3)")};
  border: 0;
  background: transparent;
  border-bottom: 2px solid
    ${({ $active }) => ($active ? "var(--ink-1)" : "transparent")};
  margin-bottom: -1px;
  font-weight: 500;
  cursor: pointer;
  &:hover {
    color: var(--ink-1);
  }
`;

export const TwoCol = styled.div`
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  align-items: start;
  @media (max-width: 1100px) {
    grid-template-columns: 1fr;
  }
`;

export const ThreeCol = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  @media (max-width: 1100px) {
    grid-template-columns: 1fr;
  }
`;

export const Barchart = styled.div`
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 180px;
  padding: 8px 0 24px;
  position: relative;
  .bar-col {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    position: relative;
    height: 100%;
    .b {
      width: 100%;
      max-width: 28px;
      background: var(--ink-2);
      border-radius: 2px 2px 0 0;
      &.alt {
        background: var(--accent);
      }
    }
    .lbl {
      font-family: var(--font-mono);
      font-size: 10px;
      color: var(--ink-3);
      position: absolute;
      bottom: -18px;
    }
  }
`;

export const Donut = styled.div`
  --p: 0;
  --c: var(--accent-mid);
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: conic-gradient(
    var(--c) calc(var(--p) * 1%),
    var(--surface-sunken) 0
  );
  display: grid;
  place-items: center;
  position: relative;
  &::before {
    content: "";
    position: absolute;
    inset: 14px;
    border-radius: 50%;
    background: var(--surface);
  }
`;

export const DonutVal = styled.span`
  position: relative;
  font-size: 22px;
  font-weight: 500;
  letter-spacing: -0.02em;
`;

export const ProdThumb = styled.div`
  width: 36px;
  height: 36px;
  border-radius: var(--radius);
  background: repeating-linear-gradient(
    135deg,
    var(--surface-sunken) 0 4px,
    var(--surface-2) 4px 8px
  );
  border: 1px solid var(--line);
  flex-shrink: 0;
`;

export const ProdThumbLg = styled.div`
  width: 100%;
  aspect-ratio: 1/1;
  border-radius: 8px;
  background: repeating-linear-gradient(
    135deg,
    var(--surface-sunken) 0 8px,
    var(--surface-2) 8px 16px
  );
  border: 1px solid var(--line);
  position: relative;
  display: grid;
  place-items: center;
  &::after {
    content: "PRODUCT IMG";
    font-family: var(--font-mono);
    font-size: 10px;
    letter-spacing: 0.2em;
    color: var(--ink-4);
    background: var(--surface);
    padding: 4px 8px;
    border-radius: 3px;
    border: 1px solid var(--line);
  }
`;

export const Empty = styled.div`
  padding: 40px;
  text-align: center;
  color: var(--ink-3);
`;

export const Divider = styled.div`
  height: 1px;
  background: var(--line);
  margin: 16px 0;
`;

export const TableFooter = styled.div`
  padding: 10px 14px;
  border-top: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--ink-3);
  background: var(--surface);
`;
