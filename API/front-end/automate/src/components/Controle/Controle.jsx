import BotaoPadrao from "../BotaoPadrao/BotaoPadrao";

export default function Controle() {
  const enviarComando = async (comando) => {
    try {
      const resposta = await fetch("/api/comando", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ acao: comando }),
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
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          height: "100vh",
          alignItems: "center",
        }}
      >
        <section style={{ marginLeft: "10rem" }}>
          <BotaoPadrao
            cor="#d32f2f"
            texto="ABRIR"
            setaDireita={true}
            onClick={() => enviarComando("abrir")}
          />
        </section>

        <section style={{ marginLeft: "-10" }}>
          <BotaoPadrao
            cor="#0edcf3ff"
            texto="FECHAR"
            setaDireita={false}
            onClick={() => enviarComando("fechar")}
          />
        </section>
      </div>
    </>
  );
}
