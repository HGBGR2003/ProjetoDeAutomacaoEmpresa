import { useState } from "react";
import { useAuth } from "../AuthContext/AuthContext";
import { useNavigate } from "react-router-dom";

export default function FormularioAcesso(params) {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [error, setError] = useState("");
  const [carregando, setCarregando] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setCarregando(true);
    try {
      const resposta = await fetch("http://localhost:8080/pessoas", {
        method: "GET",
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
      // login(dados.token);
      navigate("/controle");
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
            <section
              style={{
                marginBottom: "1rem",
                width: "100%",
                display: "flex",
                justifyContent: "center",
              }}
            >
              <input
                onChange={(e) => setEmail(e.target.value)}
                type="text"
                placeholder="&#x1F464; Usuário"
                style={{
                  fontSize: "1.5rem",
                  borderRadius: "100px",
                  padding: "0.75rem",
                  width: "80%",
                  maxWidth: "300px",
                  border: "1px solid #ccc",
                }}
              />
            </section>

            <section
              style={{
                marginBottom: "1rem",
                width: "100%",
                display: "flex",
                justifyContent: "center",
              }}
            >
              <input
                onChange={(e) => setSenha(e.target.value)}
                type="password"
                placeholder="&#x1F512; Senha"
                style={{
                  fontSize: "1.5rem",
                  borderRadius: "100px",
                  padding: "0.75rem",
                  width: "80%",
                  maxWidth: "300px",
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
                width: "80%",
                maxWidth: "300px",
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
