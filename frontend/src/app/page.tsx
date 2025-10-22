"use client";

import { Card as BlueprintCard, Button, Elevation } from "@blueprintjs/core";
import styled from "@emotion/styled";

import { useForm } from "react-hook-form";
import Form, { Filedset, Input } from "~/components/Form";

export default function Home() {
  // const formik = useFormik({
  //   initialValues: {
  //     texto: "",
  //     cor: "",
  //   },
  //   onSubmit: () => {},
  //   validationSchema: yup.object().shape({
  //     texto: yup.string().required(),
  //   }),
  // });

  const form = useForm({
    defaultValues: {
      texto: "",
      cor: "",
    },
  });

  function handleSubmit() {}

  return (
    <Card elevation={Elevation.TWO}>
      <Form form={form}>
        <Filedset legenda="Informações gerais">
          <Input name="texto" label="Texto" />
          {/* <Input name="cor" label="Cor" /> */}
        </Filedset>
        {/* <Filedset legenda="Informações básicas">
          <Input name="texto" label="Texto" />
          <Input name="cor" label="Cor" />
        </Filedset> */}
        <Button type="submit" intent="primary">
          Submit
        </Button>
        <Button type="reset" color="gray">
          Resetar
        </Button>
      </Form>
    </Card>
  );
}

const Card = styled(BlueprintCard)`
  width: fit-content;
`;
