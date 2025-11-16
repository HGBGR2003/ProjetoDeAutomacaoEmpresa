import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext/AuthContext";
import BotaoPadrao from "../BotaoPadrao/BotaoPadrao";
import PopUpRequisicao from "../PopUpRequisicao/PopUpRequisicao";
export default function Controle() {
  const { token } = useAuth();
  const [statusPortao, setStatusPortao] = useState("FECHADO");
  const [error, setError] = useState({
    message: '',
    key: 0,
  });
  const enviarComando = async (statusPorta) => {
    try {
      const resposta = await fetch("/portas", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          id: 1,
          descricao: "porta 02",
          status: statusPorta,
        }),
      });

      if (!resposta.ok) {
        throw new Error("Erro ao enviar comando");
      }
    } catch (error) {
      setError({
        message: error.message,
        key: Date.now(),
      });
    }
  };

useEffect(() => {
    const fetchStatusPortao = async () => {
      if (!token) {
        setStatusPortao("Carregando status da porta...");
        return; 
      }
      
      try {
        const resposta = await fetch("/portas/1", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        if (!resposta.ok) {
          throw new Error("Erro ao buscar status do portão");
        }
        const dados = await resposta.json();
        setStatusPortao(dados.status);
      } catch (error) {
        setError({
          message: error.message,
          key: Date.now(),
        });
      }
    };

    if (token) {
      fetchStatusPortao();
    }

    const intervalId = setInterval(fetchStatusPortao, 2000);

    return () => {
      clearInterval(intervalId);
    };
  }, [token]);

  return (
    <>
      <style>{`
        * {
          margin: 0;
          padding: 0;
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
          padding: 0px 20px;
        }

        h1 {
          font-size: 42px;
          margin-bottom: 80px;
          letter-spacing: 2px;
          text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
        }

        .botoes {
          display: flex;
          height: 70px;
          gap: 120px;
          margin-bottom: 40px;
        }

        .mensagem {
          font-size: 18px;
          color: #38bdf8;
          margin-top: 20px;
          min-height: 24px;
        }

        @media (max-width: 768px) {
          .container {
            position: absolute;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: start;
            height: 100%;
            width: 100%;
            text-align: center;
            font-family: 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #0a192f, #112240, #1e3a8a);
            color: white;
            overflow-x: hidden;
            margin: 0;
            padding: 0;
          }

          h1 {
            font-size: 32px;
            margin-top: 50px;
            margin-bottom: 50px;
            letter-spacing: 2px;
            text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
          }

          .botoes {
            display: grid;
            height: 100%;
            width: 90%;
            grid-template-rows: 1fr 1fr;
            gap: 120px;
            margin-bottom: 40px;
          }

          .mensagem {
            font-size: 18px;
            color: #38bdf8;
            margin-top: 20px;
            min-height: 24px;
          }
        }
      `}</style>

      <div className="container">
        <h1>Controle de Portão</h1>
        <span style={{fontSize:'20px', marginBottom: '30px'}}>Status atual: {statusPortao}</span>
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
        {error?.message && (
          <PopUpRequisicao
            key={error.key}
            tipo="error"
            mensagem={error?.message}
          />
        )}
      </div>
    </>
  );
}
