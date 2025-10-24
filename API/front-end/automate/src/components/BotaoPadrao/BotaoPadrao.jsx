import { useState } from "react";

export default function BotaoPadrao({cor,texto, setaDireita}) {
const [hover, setHover] = useState(false);

  return (
    <>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center"
        }}
      >
        <button
          type="button"
          style={{
            backgroundColor: cor,
            color: "white",
            border: "none",
            borderRadius: "6px",
            boxShadow: "0 2px 8px #0002",
            minWidth: "142px",
            height: "56px",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            cursor: "pointer",
            position: "relative",
            overflow: "hidden",
            fontFamily: "inherit",
            fontSize: "20px",
            padding: "0 32px",
            transition: "background 0.3s",
          }}
          onMouseEnter={() => setHover(true)}
          onMouseLeave={() => setHover(false)}
        >
          <span
            style={{
              transition: "opacity 0.3s, transform 0.3s",
              opacity: hover ? 0 : 1,
              transform: hover ? "translateX(-20px)" : "none",
              position: "absolute",
              left: setaDireita ? "55px" : "47px",
              top: 0,
              height: "100%",
              display: "flex",
              alignItems: "center",
              pointerEvents: "none",
            }}
          >
            {texto}
          </span>
          <span
            style={{
              transition: "left 0.3s",
              fontSize: "28px",
              position: "relative",
              marginLeft: hover ? "0px" : "72px",
              left: hover ? "0px" : "300px",
            }}
          >
            {setaDireita && <span>&rarr;</span>}
            {!setaDireita && <span>&larr;</span>}
          </span>
        </button>
      </div>
    </>
  );
}
