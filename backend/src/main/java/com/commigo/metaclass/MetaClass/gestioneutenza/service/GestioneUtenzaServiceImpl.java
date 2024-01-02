package com.commigo.metaclass.MetaClass.gestioneutenza.service;

import com.commigo.metaclass.MetaClass.entity.Ruolo;
import com.commigo.metaclass.MetaClass.entity.Stanza;
import com.commigo.metaclass.MetaClass.entity.StatoPartecipazione;
import com.commigo.metaclass.MetaClass.entity.Utente;
import com.commigo.metaclass.MetaClass.gestionestanza.repository.StanzaRepository;
import com.commigo.metaclass.MetaClass.gestionestanza.repository.StatoPartecipazioneRepository;
import com.commigo.metaclass.MetaClass.gestioneutenza.controller.ResponseBoolMessage;
import com.commigo.metaclass.MetaClass.gestioneutenza.repository.UtenteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("GestioneUtenzaService")
@RequiredArgsConstructor
@Slf4j    //serve per stampare delle cose nei log
@Transactional    //ogni operazione è una transazione
public class GestioneUtenzaServiceImpl implements GestioneUtenzaService{

    private final UtenteRepository utenteRepository;
    private final StatoPartecipazioneRepository statoPartecipazioneRepository;
    private final StanzaRepository stanzaRepository;
    @Override
    public boolean loginMeta(Utente u) {
        try {
            //cerca l'utente per verificare se registrato o meno
            Utente existingUser = utenteRepository.findFirstByMetaId(u.getMetaId());
           if (existingUser==null) {
                // Utente non presente nel database, lo salva
                utenteRepository.save(u);
           }
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Stampa la traccia dell'eccezione per debugging
            return false;
        }
    }

    /**
     * @param Id
     * @param dataMap
     * @param u
     * @return
     */
    @Override
    public ResponseBoolMessage modificaDatiUtente(Long Id, Map<String, Object> dataMap, Utente u) {

        try {
            Utente existingUser = utenteRepository.findUtenteById(Id);
            if(existingUser == null) {
                return new ResponseBoolMessage(false, "l'utente non esiste");
            }else{
                if(utenteRepository.updateAttributes(Id, dataMap)>0){
                    u = utenteRepository.findUtenteById(Id);
                    return new ResponseBoolMessage(true, "modifica effettuata con successo");
                }else{
                    u = existingUser;
                    return new ResponseBoolMessage(true, "nessuna modifica effettuata");
                }
            }
        }catch (Exception e) {
            e.printStackTrace(); // Stampa la traccia dell'eccezione per debugging
            return new ResponseBoolMessage(false, "errore nella modifica dei dati");
        }
    }

    /**
     * @param MetaId
     * @return
     */
    @Override
    public List<Stanza> getStanzeByUserId(String MetaId) {
        try {
            Utente existingUser = utenteRepository.findFirstByMetaId(MetaId);
            if(existingUser == null) {
                throw new Exception("Utente non presente nel database");
            }else{
                List<StatoPartecipazione> stati =
                        statoPartecipazioneRepository.findAllByUtente(existingUser);
                if(stati==null){
                    throw new Exception("Errore nella ricerca delle stanze");
                }else{
                    // Estrai gli attributi 'stanza' dalla lista 'stati' e messi in una nuova lista
                    return stati.stream()
                            .map(StatoPartecipazione::getStanza)
                            .collect(Collectors.toList());
                }
            }
        }catch (Exception e) {
            e.printStackTrace(); // Stampa la traccia dell'eccezione per debugging
            return null;        }
    }

    @Override
    public ResponseBoolMessage upgradeUtente(String id_Uogm, long id_Uog, long id_stanza){

        Utente ogm = utenteRepository.findFirstByMetaId(id_Uogm);
        Utente og = utenteRepository.findUtenteById(id_Uog);
        Stanza stanza = stanzaRepository.findStanzaById(id_stanza);

        StatoPartecipazione stato_ogm = statoPartecipazioneRepository.findStatoPartecipazioneByUtenteAndStanza(ogm, stanza);
        if(stato_ogm.getRuolo().getNome().equalsIgnoreCase("Organizzatore_Master")){
            StatoPartecipazione stato_og = statoPartecipazioneRepository.findStatoPartecipazioneByUtenteAndStanza(og, stanza);
            if(stato_og.getRuolo().getNome().equalsIgnoreCase("Partecipante")){
                stato_og.getRuolo().setNome(Ruolo.ORGANIZZATORE);
                return new ResponseBoolMessage(true, "L'utente selezionato ora è un organizzatore");

            }else if (stato_og.getRuolo().getNome().equalsIgnoreCase("Organizzatore")){
                return new ResponseBoolMessage(false, "L'utente selezionato è già un'organizzatore");

            }else{
                return new ResponseBoolMessage(false, "L'utente selezionato ora non può essere declassato ad organizzatore");
            }
        }else{
            return new ResponseBoolMessage(false, "Non puoi promuovere un'utente perché non sei un'organizzatore master");
        }
    }
}
