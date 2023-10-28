import {Route, Routes} from "react-router-dom";
import React from "react";
import {Home} from "./components/home/Home";
import {EmailComponent} from "./components/email/EmailComponent";

export default function RoutesComponent() {
    return <Routes>
        <Route path={"/email/:messageId"} element={<EmailComponent/>}/>
        <Route path={"/"} element={<Home/>}/>
    </Routes>
}