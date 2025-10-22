import { useState } from "react";

export default function FormularioAcesso(params) {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [error, setError] = useState("");
  const [carregando, setCarregando] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const resposta = await fetch("", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email,
          senha,
        }),
      });

      if (!resposta.ok) {
        throw new Error("Login Inválido");
      }

      const dados = await resposta.json();
      console.log(dados);
      localStorage.setItem("token", dados.token);
    } catch (error) {
      setError(error.message);
    } finally {
      setCarregando(false);
    }
  };

  return (
    <>
      <>
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            minHeight: "100vh",
            backgroundColor: "#007BFF", // azul de fundo
            padding: "2rem",
            boxSizing: "border-box",
          }}
        >
          <h1 style={{ color: "#ffffff" }}>
            Formulário de Acesso Para Empresas
          </h1>

          <form
            onSubmit={handleSubmit}
            style={{
              display: "grid",
              justifyItems: "center",
              alignContent: "center",
              width: "100%",
              maxWidth: "400px",
              backgroundColor: "#ffffff",
              padding: "2rem",
              borderRadius: "1rem",
              boxShadow: "0 4px 8px #00000033",
            }}
          >
            <section style={{ marginBottom: "1rem", width: "100%" }}>
              <input
                type="text"
                placeholder="&#x1F464; Usuário"
                style={{
                  fontSize: "1.5rem",
                  borderRadius: "100px",
                  padding: "0.75rem",
                  width: "100%",
                  border: "1px solid #ccc",
                }}
              />
            </section>

            <section style={{ marginBottom: "1rem", width: "100%" }}>
              <input
                type="password"
                placeholder="&#x1F512; Senha"
                style={{
                  fontSize: "1.5rem",
                  borderRadius: "100px",
                  padding: "0.75rem",
                  width: "100%",
                  border: "1px solid #ccc",
                }}
              />
            </section>

            <button
              type="submit"
              style={{
                borderRadius: "2rem",
                padding: "1rem",
                backgroundColor: "#000000",
                color: "#ebebeb",
                fontSize: "1rem",
                width: "100%",
                cursor: "pointer",
              }}
            >
              Enviar
            </button>
          </form>
        </div>
      </>
    </>
  );
}
