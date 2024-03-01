"use client";

import { Tree, TreeEventHandler, TreeNodeInfo } from "@blueprintjs/core";
import { useState } from "react";
import "./nav.css";

const menus: Array<TreeNodeInfo> = [
  {
    id: "manutencao",
    label: "Manutenção",
    icon: "add",
    childNodes: [{ id: "manutencao-produtos", label: "Produtos" }],
  },
];

export default function Nav() {
  const [keysAbertas, setKeysAbertas] = useState<string[]>([]);

  const handleNode: TreeEventHandler = (node) => {
    const key = `${node.id}`;

    if (node.childNodes?.length) {
      if (keysAbertas.includes(key)) {
        setKeysAbertas((prev) => {
          prev.splice(prev.indexOf(key), 1);
          return [...prev];
        });
      } else setKeysAbertas((prev) => [...prev, key]);
    }
  };

  return (
    <nav>
      <Tree
        onNodeExpand={handleNode}
        onNodeCollapse={handleNode}
        onNodeClick={handleNode}
        contents={menus.map((menu) => ({
          ...menu,
          className: "cursor-pointer",
          isExpanded: keysAbertas.includes(`${menu.id}`),
        }))}
      />
    </nav>
  );
}
