import React, { ReactElement } from "react";
import { Navigate } from "react-router-dom";

type Props = {
  children: ReactElement;
};

const PrivateRoute = ({ children }: Props): ReactElement => {
  // Controlla sia authToken che username per compatibilità
  const isAuthenticated = Boolean(
    localStorage.getItem("authToken") || localStorage.getItem("username")
  );
  
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

export default PrivateRoute;