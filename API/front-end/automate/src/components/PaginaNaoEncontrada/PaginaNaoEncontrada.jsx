import { Link } from "react-router-dom";

export default function PaginaNaoEncontrada() {
  return (
    <>
      <h1
        style={{
          position: "absolute",
          paddingBottom: "50px",
          display: "flex",
          justifySelf: "center",
        }}
      >
        Página Não Encontrada
      </h1>
      <div
        style={{
          width: "100%",
          height: "100vh",
          justifyItems: "center",
          alignContent: "center",
        }}
      >
        <Link to={"/"}>
          <button
            type="button"
            style={{
              borderRadius: "100px",
              width: "15%",
              minWidth: "300px",
              maxWidth: "500px",
              height: "50px",
            }}
          >
            Voltar para a Página Inicial
          </button>
        </Link>
      </div>
    </>
  );
}
