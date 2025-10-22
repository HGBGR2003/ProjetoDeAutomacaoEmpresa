import { Navigate } from "react-router-dom";
import { useAuth } from "../AuthContext/AuthContext";

const ProtectedRouter = ({ children }) => {
  const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return children;
}
export default ProtectedRouter;