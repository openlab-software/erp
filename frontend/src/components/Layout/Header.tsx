"use client";
import { EntityTitle, Switch } from "@blueprintjs/core";
import { DoughnutChart } from "@blueprintjs/icons";
import { useEffect } from "react";

export default function Header() {
  useEffect(() => {
    function setTema(value: "light" | "dark" = "light") {
      if (value === "dark") {
        document.documentElement.classList.add("dark");
        document.documentElement.classList.add("bp5-dark");
      } else {
        document.documentElement.classList.remove("dark");
        document.documentElement.classList.remove("bp5-dark");
      }
    }

    function handleStorageChange(e: StorageEvent) {
      console.log({ e });
      setTema("dark" === localStorage.getItem("theme") ? "dark" : "light");
    }

    window.addEventListener("storage", handleStorageChange);
    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  return (
    <header className="bg-white dark:bg-[#383e47] sticky z-10 w-full top-0 h-[81px] flex p-4 border-b border-[rgb(227, 232, 237)] dark:border-[rgba(17,20,24,.4)] justify-between">
      <EntityTitle
        title="patrick.dev.br"
        icon={<DoughnutChart className="text-blue-500" size={20} />}
        className="font-bold text-xl"
      />
      <Switch
        onChange={(event) => {
          localStorage.setItem(
            "theme",
            event.target.checked ? "dark" : "light"
          );
          window.dispatchEvent(new StorageEvent("storage"));
        }}
      />
    </header>
  );
}
