import { Route, Routes } from "react-router-dom";
import "./App.css";
import FormularioAcesso from "./components/formularioDeAcesso/FormularioAcesso";
import PaginaNaoEncontrada from "./components/PaginaNaoEncontrada/PaginaNaoEncontrada";
import Controle from "./components/Controle/Controle";
import ProtectedRouter from "./components/ProtectedRouter/ProtectedRouter";

function App() {
  return (
    <div className="App">
      <Routes>
        <Route path="/" element={<FormularioAcesso />} />
        <Route
          path="/controle"
          element={
            <ProtectedRouter>
              <Controle />
            </ProtectedRouter>
          }
        />
        <Route path="*" element={<PaginaNaoEncontrada />} />
      </Routes>
    </div>
  );
}

export default App;
