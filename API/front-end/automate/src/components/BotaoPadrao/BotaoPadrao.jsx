import { useState } from "react";
export default function Botao({ texto, cor, direcao, aoClicar }) {
  const [hover, setHover] = useState(false);
  return (
    <>
      <style>{`
        .botao {
          position: relative;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 10px;
          border: none;
          border-radius: 12px;
          box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
          color: white;
          font-size: 20px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.3s ease;
          overflow: hidden;
          min-width: 160px;
          backdrop-filter: blur(6px);
        }

        .botao:hover {
          transform: scale(1.05);
          box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
        }

        .botao:active {
          transform: scale(0.97);
        }

        .botao::after {
          content: "";
          position: absolute;
          inset: 0;
          background: rgba(255, 255, 255, 0.15);
          border-radius: 12px;
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        .botao:hover::after {
          opacity: 1;
        }

        .texto {
          transition: all 0.3s ease;
        }

        .texto.sumir {
          opacity: 0;
          transform: translateX(-20px);
        }

        .seta {
          font-size: 26px;
          opacity: 0;
          transform: translateX(40px);
          transition: all 0.3s ease;
          position: relative;
        }

        .seta.mostrar {
          opacity: 1;
          transform: translateX(0);
        }
      `}</style>

      <button
        className="botao"
        style={{ backgroundColor: cor }}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
        onClick={aoClicar}
      >
        <span className={`texto ${hover ? "sumir" : ""}`}>{texto}</span>
        <span className={`seta ${hover ? "mostrar" : ""}`}>{direcao}</span>
      </button>
    </>
  );
}
