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
      <h1>Formulario de Acesso Para Empresas</h1>
      <form onSubmit={handleSubmit} style={{
        justifyItems:"center",
        alignContent:"center",
        width:"100%",
      }}>
        <div style={{
            display:"grid"
        }}>
          <section
            style={{
              display: "inline-flex",
              marginBottom: "3rem",
            }}
          >
            <input type="text" placeholder="Usuário" style={{
                width:"300px",
                height:"70px",
                fontSize: "2rem",
                borderRadius:"100px"
            }} />
          </section>

          <section
            style={{
              display: "inline-flex",
            }}
          >
            <input type="password" placeholder="Senha" style={{
                width:"300px",
                height:"70px",
                fontSize: "2rem",
                borderRadius:"100px"
            }} />
          </section>

         <button type="submit">Enviar</button>

        </div>
      </form>
    </>
  );
}
