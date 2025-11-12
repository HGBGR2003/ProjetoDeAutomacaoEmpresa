import { useAuth } from "../AuthContext/AuthContext";
import BotaoPadrao from "../BotaoPadrao/BotaoPadrao";
export default function Controle() {
  const {token} = useAuth();
  const enviarComando = async () => {
    try {
      const resposta = await fetch("http://localhost:8080/portas", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({
          id:1,
          descricao: "porta 02",
          status: "ABERTO",
        }),
      });

      if (!resposta.ok) {
        throw new Error("Erro ao enviar comando");
      }

      const resultado = await resposta.json();
      console.log("Resposta do servidor:", resultado);
    } catch (error) {
      console.error("Erro:", error.message);
    }
  };

  return (
    <>
      <style>{`
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }

        .container {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100vh;
          text-align: center;
          font-family: 'Segoe UI', sans-serif;
          background: linear-gradient(135deg, #0a192f, #112240, #1e3a8a);
          color: white;
        }

        h1 {
          font-size: 42px;
          margin-bottom: 80px;
          letter-spacing: 2px;
          text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
        }

        .botoes {
          display: flex;
          gap: 120px;
          margin-bottom: 40px;
        }

        .mensagem {
          font-size: 18px;
          color: #38bdf8;
          margin-top: 20px;
          min-height: 24px;
        }
      `}</style>

      <div className="container">
        <h1>Controle de Portão</h1>

        <div className="botoes">
          <BotaoPadrao
            texto="ABRIR"
            cor="#d32f2f"
            direcao="→"
            aoClicar={() => enviarComando("ABERTO")}
          />
          <BotaoPadrao
            texto="FECHAR"
            cor="#0edcf3"
            direcao="←"
            aoClicar={() => enviarComando("FECHADO")}
          />
        </div>
      </div>
    </>
  );
}
