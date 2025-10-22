import { Button } from "~/components/ui/button";

export default async function Page() {
  return (
    <div className="flex flex-col gap-2 p-5">
      {/* Hello World! */}
      <Button variant="default">oi</Button>
      <Button variant="destructive">destructive</Button>
      <Button variant="ghost">ghost</Button>
      <Button variant="link">link</Button>
      <Button variant="outline">outline</Button>
      <Button variant="secondary">secondary</Button>
    </div>
  );
}
