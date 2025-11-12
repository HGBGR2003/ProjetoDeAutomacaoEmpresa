import { useEffect, useState } from "react";

const PopUpRequisicao = ({ tipo, mensagem }) => {
  const [popup, setPopup] = useState({ visible: false, message: "", type: "" });

  useEffect(() => {
    if (mensagem && tipo) {
      setPopup({ visible: true, message: mensagem, type: tipo });

      const timer = setTimeout(() => {
        setPopup({ visible: false, message: "", type: "" });
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [mensagem, tipo]);

  if (!popup.visible) return null;

  return (
    <div
      style={{
        padding: "10px",
        marginTop: "10px",
        borderRadius: "5px",
        color: "#fff",
        backgroundColor: popup.type === "success" ? "green" : "red",
      }}
    >
      {popup.message}
    </div>
  );
};

export default PopUpRequisicao
