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
      login(dados.token);
      navigate("/controle");
    } catch (error) {
      setError(error.message);
    } finally {
      setCarregando(false);
    }
  };

  return (
    <>
      <h1>Formulario de Acesso Para Empresas</h1>
      <form
        onSubmit={handleSubmit}
        style={{
          justifyItems: "center",
          alignContent: "center",
          width: "100%",
        }}
      >
        <div
          style={{
            display: "grid",
          }}
        >
          <section
            style={{
              display: "inline-flex",
              marginBottom: "3rem",
            }}
          >
            <input
              type="text"
              placeholder="Usuário"
              onChange={(e) => setEmail(e.targer.value)}
              style={{
                width: "300px",
                height: "70px",
                fontSize: "2rem",
                borderRadius: "100px",
              }}
            />
          </section>

          <section
            style={{
              display: "inline-flex",
            }}
          >
            <input
              type="password"
              placeholder="Senha"
              onChange={(e) => setSenha(e.targer.value)}
              style={{
                width: "300px",
                height: "70px",
                fontSize: "2rem",
                borderRadius: "100px",
              }}
            />
          </section>

          <button type="submit">Enviar</button>
        </div>
      </form>
    </>
  );
}
