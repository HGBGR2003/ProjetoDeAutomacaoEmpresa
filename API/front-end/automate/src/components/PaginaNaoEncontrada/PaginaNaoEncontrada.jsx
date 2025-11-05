import { Link } from "react-router-dom";

export default function PaginaNaoEncontrada() {
  return (
    <>
      <style>{`
        body {
          margin: 0;
          background-color: #0a192f; 
          font-family: 'Segoe UI', sans-serif;
          color: #f8fafc;
          height: 100vh;
          overflow: hidden;
        }

        .pagina-erro {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100vh;
          text-align: center;
          animation: fadeIn 0.8s ease-in-out;
        }

        h1 {
          font-size: 96px;
          font-weight: 800;
          color: #ef4444; 
          margin: 0;
          text-shadow: 0 2px 12px rgba(239, 68, 68, 0.5);
        }

        p {
          font-size: 22px;
          color: #cbd5e1;
          margin-top: 10px;
          margin-bottom: 40px;
          letter-spacing: 1px;
        }

        .botao-voltar {
          background-color: #1e40af;
          color: white;
          border: none;
          border-radius: 40px;
          padding: 14px 36px;
          font-size: 18px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.3s ease;
          box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
        }

        .botao-voltar:hover {
          background-color: #3b82f6; 
          transform: scale(1.07);
          box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(15px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>

      <div className="pagina-erro">
        <h1>404</h1>
        <p>Página Não Encontrada</p>

        <Link to="/">
          <button className="botao-voltar">Voltar para a Página Inicial</button>
        </Link>
      </div>
    </>
  );
}
