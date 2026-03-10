import at.ac.tgm.entity.BootstickEntity;
import at.ac.tgm.entity.DatenstickEntity;
import at.ac.tgm.enums.Schulklasse;
import at.ac.tgm.enums.Status;
import at.ac.tgm.repository.BootstickRepository;
import at.ac.tgm.repository.DatenstickRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BootstickRepository bootstickRepository;
    @Autowired
    private DatenstickRepository datenstickRepository;

    @Override
    public void run(String... args) {

        if (bootstickRepository.count() == 0) {

            Schulklasse[] klassen = Schulklasse.values();

            for (Schulklasse k : klassen) {
                for (int i = 1; i <= 40; i++) {

                    BootstickEntity stick = new BootstickEntity();

                    stick.setName(k);
                    stick.setNummer(i);
                    stick.setStatus(Status.VORHANDEN);

                    bootstickRepository.save(stick);
                }
            }
        }
        if (datenstickRepository.count() == 0) {
            Schulklasse[] klassen = Schulklasse.values();
            for (Schulklasse k : klassen) {
                for (int i = 1; i <= 40; i++) {

                    DatenstickEntity stick = new DatenstickEntity();

                    stick.setName(k);
                    stick.setNummer(i);
                    stick.setStatus(Status.VORHANDEN);

                    datenstickRepository.save(stick);
                }
            }
        }
    }
}