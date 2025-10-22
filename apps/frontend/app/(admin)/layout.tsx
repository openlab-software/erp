import { PropsWithChildren } from "react";

export default function AdminLayout(props: PropsWithChildren) {
  return (
    <div className="flex flex-col">
      <header></header>

      <div>
        <aside></aside>

        <main>{props.children}</main>
      </div>
    </div>
  );
}
