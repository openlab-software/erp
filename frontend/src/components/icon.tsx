interface IconProps {
  name: string;
  size?: number;
  className?: string;
}

export function Icon({ name, size = 16, className = "" }: IconProps) {
  const props = {
    width: size,
    height: size,
    viewBox: "0 0 16 16",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.5,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    className,
  };
  switch (name) {
    case "dashboard":
      return (
        <svg {...props}>
          <rect x="2" y="2" width="5" height="5" rx="1" />
          <rect x="9" y="2" width="5" height="5" rx="1" />
          <rect x="2" y="9" width="5" height="5" rx="1" />
          <rect x="9" y="9" width="5" height="5" rx="1" />
        </svg>
      );
    case "box":
      return (
        <svg {...props}>
          <path d="M8 2 L14 5 L8 8 L2 5 Z" />
          <path d="M2 5 V11 L8 14 V8" />
          <path d="M14 5 V11 L8 14" />
        </svg>
      );
    case "stack":
      return (
        <svg {...props}>
          <path d="M2 5 L8 2 L14 5 L8 8 Z" />
          <path d="M2 8 L8 11 L14 8" />
          <path d="M2 11 L8 14 L14 11" />
        </svg>
      );
    case "cart":
      return (
        <svg {...props}>
          <path d="M1 2 H3 L4.5 11 H13 L14.5 4 H4" />
          <circle cx="6" cy="13.5" r="0.8" />
          <circle cx="12" cy="13.5" r="0.8" />
        </svg>
      );
    case "wallet":
      return (
        <svg {...props}>
          <rect x="2" y="3.5" width="12" height="9" rx="1.5" />
          <path d="M2 6 H14" />
          <circle cx="11.5" cy="9.5" r="0.7" />
        </svg>
      );
    case "users":
      return (
        <svg {...props}>
          <circle cx="6" cy="6" r="2.5" />
          <path d="M2 13 C2 10.5 4 9 6 9 C8 9 10 10.5 10 13" />
          <circle cx="11" cy="6.5" r="2" />
          <path d="M11 9 C12.5 9 14 10 14 12" />
        </svg>
      );
    case "factory":
      return (
        <svg {...props}>
          <path d="M2 14 V7 L6 9 V7 L10 9 V5 L14 7 V14 Z" />
          <path d="M5 14 V11" />
          <path d="M8 14 V11" />
          <path d="M11 14 V11" />
        </svg>
      );
    case "chart":
      return (
        <svg {...props}>
          <path d="M2 13 H14" />
          <path d="M4 13 V9" />
          <path d="M7 13 V5" />
          <path d="M10 13 V7" />
          <path d="M13 13 V3" />
        </svg>
      );
    case "settings":
      return (
        <svg {...props}>
          <circle cx="8" cy="8" r="2.5" />
          <path d="M8 1 V3 M8 13 V15 M1 8 H3 M13 8 H15 M3 3 L4.5 4.5 M11.5 11.5 L13 13 M3 13 L4.5 11.5 M11.5 4.5 L13 3" />
        </svg>
      );
    case "menu":
      return (
        <svg {...props}>
          <path d="M2 4 H14 M2 8 H14 M2 12 H14" />
        </svg>
      );
    case "bell":
      return (
        <svg {...props}>
          <path d="M3.5 11 H12.5 L11 9 V6.5 C11 4.5 9.5 3 8 3 C6.5 3 5 4.5 5 6.5 V9 Z" />
          <path d="M6.5 13 C6.5 13.8 7.2 14.5 8 14.5 C8.8 14.5 9.5 13.8 9.5 13" />
        </svg>
      );
    case "plus":
      return (
        <svg {...props}>
          <path d="M8 3 V13 M3 8 H13" />
        </svg>
      );
    case "minus":
      return (
        <svg {...props}>
          <path d="M3 8 H13" />
        </svg>
      );
    case "filter":
      return (
        <svg {...props}>
          <path d="M2 3 H14 L9.5 8 V13 L6.5 11.5 V8 Z" />
        </svg>
      );
    case "download":
      return (
        <svg {...props}>
          <path d="M8 2 V11 M4.5 7.5 L8 11 L11.5 7.5 M2.5 13.5 H13.5" />
        </svg>
      );
    case "upload":
      return (
        <svg {...props}>
          <path d="M8 11 V2 M4.5 5.5 L8 2 L11.5 5.5 M2.5 13.5 H13.5" />
        </svg>
      );
    case "more":
      return (
        <svg {...props}>
          <circle cx="3.5" cy="8" r="0.8" />
          <circle cx="8" cy="8" r="0.8" />
          <circle cx="12.5" cy="8" r="0.8" />
        </svg>
      );
    case "edit":
      return (
        <svg {...props}>
          <path d="M11 2.5 L13.5 5 L5 13.5 L2 14 L2.5 11 Z" />
        </svg>
      );
    case "trash":
      return (
        <svg {...props}>
          <path d="M2.5 4 H13.5 M5 4 V2.5 H11 V4 M4 4 V14 H12 V4 M7 7 V11 M9 7 V11" />
        </svg>
      );
    case "check":
      return (
        <svg {...props}>
          <path d="M3 8 L7 12 L13 4" />
        </svg>
      );
    case "x":
      return (
        <svg {...props}>
          <path d="M4 4 L12 12 M12 4 L4 12" />
        </svg>
      );
    case "arrow-right":
      return (
        <svg {...props}>
          <path d="M3 8 H13 M9 4 L13 8 L9 12" />
        </svg>
      );
    case "arrow-down":
      return (
        <svg {...props}>
          <path d="M8 3 V13 M4 9 L8 13 L12 9" />
        </svg>
      );
    case "arrow-up":
      return (
        <svg {...props}>
          <path d="M8 13 V3 M4 7 L8 3 L12 7" />
        </svg>
      );
    case "calendar":
      return (
        <svg {...props}>
          <rect x="2" y="3.5" width="12" height="11" rx="1" />
          <path d="M2 6.5 H14 M5 2 V5 M11 2 V5" />
        </svg>
      );
    case "search":
      return (
        <svg {...props}>
          <circle cx="7" cy="7" r="4" />
          <path d="M13.5 13.5 L10 10" />
        </svg>
      );
    case "refresh":
      return (
        <svg {...props}>
          <path d="M2 8 A6 6 0 0 1 13 5 M14 2 V5 H11" />
          <path d="M14 8 A6 6 0 0 1 3 11 M2 14 V11 H5" />
        </svg>
      );
    case "warehouse":
      return (
        <svg {...props}>
          <path d="M2 14 V7 L8 3 L14 7 V14 Z" />
          <path d="M5 14 V10 H11 V14" />
          <path d="M5 12 H11" />
        </svg>
      );
    case "transfer":
      return (
        <svg {...props}>
          <path d="M2 5 H12 M9 2 L12 5 L9 8" />
          <path d="M14 11 H4 M7 8 L4 11 L7 14" />
        </svg>
      );
    case "chevron-right":
      return (
        <svg {...props}>
          <path d="M6 3 L11 8 L6 13" />
        </svg>
      );
    case "chevron-down":
      return (
        <svg {...props}>
          <path d="M3 6 L8 11 L13 6" />
        </svg>
      );
    case "external":
      return (
        <svg {...props}>
          <path d="M9 3 H13 V7 M13 3 L8 8 M11 9 V13 H3 V5 H7" />
        </svg>
      );
    default:
      return null;
  }
}
