import React, { useEffect, useState } from "react";
import { MyHeader } from "../components/Layout/Header/Header";
import { MyFooter } from "../components/Layout/Footer/Footer";
import { useParams } from "react-router-dom";
import CalendarComp from "../components/Forms/ScheduleMeetingForm/CalendarComp";
import { checkRole } from "../functions/checkRole";
import UserListInRoom from "../components/Lists/UserList/UserListInRoom";
import { faChalkboardUser } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import MyModifyForm from "../components/Forms/ModifyRoomForm/MyModifyForm";
import MeetingList from "../components/Calendar/CalendarViewer";
import RequestSection from "../components/Forms/AccessRequest/RequestSection";
import SelectScenario from "../components/Forms/SelectNewScenario/SelectScenario";
import BannedUserList from "../components/Lists/UserList/BannedUserList";
import MeetinginRoon from "../components/Lists/MeetingList/MeetinginRoon";

export const SingleRoom = () => {
    const { id: id_stanza } = useParams();
    const [role, setRole] = useState("Partecipante"); // Default role value
    const [stanzaSingola, setStanzaSingola] = useState("");
    const [isOrganizer, setIsOrganizer] = useState(false);

    sessionStorage.setItem("idStanza", id_stanza);

    useEffect(() => {
        fetchSingleRoom();
        // eslint-disable-next-line
    }, []);

    const requestOption = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + sessionStorage.getItem("token")
        },
    };

    const fetchSingleRoom = async () => {
        try {
            const response = await fetch(`http://localhost:8080/visualizzaStanza/${id_stanza}`, requestOption);

            if (!response.ok) {
                throw new Error('Errore nel recupero degli scenari.');
            }

            const data = await response.json();

            setStanzaSingola(data.value);
            console.log("Stanza singola;", data.value);
        } catch (error) {
            console.error('Errore durante il recupero degli scenari:', error.message);
        }
    };

    useEffect(() => {
        const fetchDataAndResize = async () => {
            try {
                const fetchedRole = await checkRole(id_stanza);
                setRole(fetchedRole);
                // Set isOrganizer based on the fetched role
                setIsOrganizer(fetchedRole === "Organizzatore" || fetchedRole === "Organizzatore_Master");
            } catch (error) {
                console.error(error);
                // Handle error fetching role, set role state accordingly if needed
            }
        };

        fetchDataAndResize(); // Fetch the role when the component mounts

        window.addEventListener("resize", resizeSideNav);
        return () => {
            window.removeEventListener("resize", resizeSideNav);
        };
    }, [id_stanza]); // Run effect only when id_stanza changes

    useEffect(() => {
        resizeSideNav(); // Resize side nav when the role changes
    }, [role, isOrganizer]); // Run effect when role or isOrganizer changes

    const resizeSideNav = () => {
        const mainSection = document.querySelector(".roomSec");
        const sideNav = document.querySelector(".side-nav");

        if (mainSection && sideNav) {
            const mainHeight = window.getComputedStyle(mainSection).height;
            sideNav.style.maxHeight = mainHeight;
        }
    };

    const isOrg = () => {
        return isOrganizer;
    };

    return (
        <>
            <header>
                <MyHeader />
            </header>
            <main>
                <section className={"roomSec"} id={"rSec"}>
                    <FontAwesomeIcon
                        icon={faChalkboardUser}
                        size="4x"
                        style={{ color: "#c70049" }}
                    />
                    <h1>{stanzaSingola.nome}</h1>
                    <div style={{
                        border: '2px solid #ccc',
                        borderRadius: '8px',
                        padding: '10px',
                        boxShadow: '0 0 10px rgba(0, 0, 0, 0.1)',
                        maxWidth: "30vw",
                        marginInline: "auto",
                        marginBottom: "10px"
                    }}>
                        <h2>CODICE STANZA: {stanzaSingola.codice}</h2>
                        <h4>
                            SCENARIO SELEZIONATO: {stanzaSingola && stanzaSingola.scenario && stanzaSingola.scenario.nome}
                        </h4>
                    </div>
                    <div
                        className={"masterDiv"}
                    >
                        <MeetingList />
                        {isOrg() && (
                            <>
                                <div className={"childDiv"}>
                                    <h2>Funzioni organizzatore:</h2>
                                    <CalendarComp />
                                    <MyModifyForm />
                                    <SelectScenario Id_stanza={id_stanza} />
                                    <RequestSection id_stanza={id_stanza} />
                                    <BannedUserList id_stanza={id_stanza} />
                                </div>
                            </>
                        )}
                    </div>
                </section>
                <aside className="side-nav">
                    <div className={"childDiv"}>
                        <UserListInRoom />
                        <MeetinginRoon />
                    </div>
                </aside>
            </main>
            <footer>
                <MyFooter />
            </footer>
        </>
    );
};
