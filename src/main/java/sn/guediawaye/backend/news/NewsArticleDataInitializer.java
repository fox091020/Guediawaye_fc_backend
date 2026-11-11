package sn.guediawaye.backend.news;

import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NewsArticleDataInitializer implements ApplicationRunner {

    private final NewsArticleRepository articles;
    private final NewsArticleSeedStateRepository seedState;

    public NewsArticleDataInitializer(
            NewsArticleRepository articles,
            NewsArticleSeedStateRepository seedState) {
        this.articles = articles;
        this.seedState = seedState;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (seedState.existsById(1L)) {
            return;
        }
        if (articles.count() == 0) {
            articles.saveAll(defaultArticles());
        }
        seedState.save(new NewsArticleSeedState(1L));
    }

    private List<NewsArticle> defaultArticles() {
        return List.of(
                article("groupe-championnat", "Équipe", "Le groupe se prépare pour le prochain match du championnat",
                        "Les Crabes intensifient leur préparation avant une nouvelle rencontre importante du championnat.",
                        "Après une semaine de travail soutenu, le groupe du Guediawaye FC poursuit sa préparation avec sérieux et détermination.\n\nLe staff technique insiste sur la concentration et la solidarité du collectif.",
                        "/bg-match.jpeg", "2025-10-25T12:00:00Z", false),
                article("seance-entrainement", "Entraînement", "Séance d’entraînement : intensité et concentration",
                        "Une séance intense pour préparer les prochaines échéances.",
                        "Les joueurs ont travaillé les automatismes et la maîtrise du ballon lors d’une séance particulièrement engagée.\n\nLe groupe retrouvera le terrain pour une nouvelle séance dans les prochains jours.",
                        "/hero-background-2.png", "2025-10-22T12:00:00Z", false),
                article("supporters-rendez-vous", "Supporters", "Les supporters toujours au rendez-vous !",
                        "Dans les tribunes comme en déplacement, les Crabes peuvent compter sur leur famille.",
                        "La ferveur des supporters accompagne chaque rencontre du Guediawaye FC. Leur énergie et leur fidélité sont une force essentielle pour le club.\n\nLe prochain rendez-vous sera une nouvelle occasion de partager cette passion.",
                        "/histoire-image.png", "2025-10-18T12:00:00Z", false),
                article("interview-coach", "Interview", "Le coach : « On travaille pour atteindre nos objectifs »",
                        "Le staff technique revient sur le travail du groupe et les ambitions de la saison.",
                        "Le coach souligne l’importance du travail quotidien et de la discipline collective.\n\nLe message est clair : rester concentrés, garder l’unité du groupe et avancer avec ambition.",
                        "/hero-club.jpg", "2025-10-15T12:00:00Z", false),
                article("stade-municipal", "Événement", "Le stade municipal au cœur des prochains événements du club",
                        "Le club rassemble joueurs, supporters et familles pour une journée ouverte à la communauté.",
                        "Le stade municipal accueille une journée de rencontre dédiée aux supporters et aux familles de Guediawaye. Au programme : échanges avec les membres du club, animations et moments de convivialité autour des couleurs des Crabes.\n\nLe Guediawaye FC remercie les bénévoles et les participants qui contribuent à faire vivre cette initiative.",
                        "/bg_equipe.jpeg", "2025-10-12T12:00:00Z", false),
                article("victoire-en-championnat", "Équipe", "Le Guediawaye FC enchaine une nouvelle victoire en championnat",
                        "Les Crabes poursuivent leur parcours en championnat avec une nouvelle victoire collective.",
                        "Le Guediawaye FC a livré une prestation engagée et a su faire la différence grâce à la solidarité de son groupe.\n\nLe staff et les joueurs remercient les supporters pour leur présence et leur soutien.",
                        "/actu_1.jpeg", "2025-10-12T11:00:00Z", true),
                article("journee-club", "Événement", "Une journée de solidarité avec les familles du quartier",
                        "Une journée de partage réunit le club et les familles de Guediawaye.",
                        "Le Guediawaye FC organise une journée de solidarité et de partage avec les familles du quartier. Cette initiative permet aux membres du club et aux habitants de se retrouver dans une ambiance chaleureuse.\n\nLe club adresse ses remerciements à toutes les personnes mobilisées pour préparer cette rencontre.",
                        "/hero-background.png", "2025-10-08T12:00:00Z", false),
                article("dernier-match-us-goree", "Événement", "Retour sur notre dernier match face à l’US Gorée",
                        "Retour en images et en détails sur la dernière rencontre des Crabes face à l’US Gorée.",
                        "La rencontre a été disputée avec intensité, dans une ambiance portée par les supporters.\n\nRetrouvez les temps forts de cette opposition et les réactions du groupe.",
                        "/actu_2.webp", "2025-10-05T12:00:00Z", true),
                article("formation-avenir", "Événement", "Le club ouvre ses portes aux jeunes talents de Guediawaye",
                        "Une rencontre avec les jeunes de la ville met à l’honneur la formation et la pratique du football.",
                        "Le Guediawaye FC accueille les jeunes talents de la ville pour un temps de découverte et d’échange autour du football. Les éducateurs présentent les valeurs du club et l’importance du travail collectif.\n\nCette initiative encourage les jeunes à poursuivre leur pratique sportive et à s’impliquer dans la vie du club.",
                        "/img_joueur.jpg", "2025-10-05T11:00:00Z", false),
                article("boutique-officielle", "Vie du club", "Le club lance sa boutique officielle !",
                        "Les supporters peuvent désormais retrouver les produits officiels aux couleurs des Crabes.",
                        "Maillots, vêtements et accessoires permettent à chacun d’afficher son attachement au Guediawaye FC.\n\nCette initiative contribue à rapprocher le club de sa communauté et à soutenir son développement.",
                        "/actu_3.png", "2025-09-28T11:00:00Z", false));
    }

    private NewsArticle article(
            String slug,
            String category,
            String title,
            String summary,
            String content,
            String imageUrl,
            String publishedAt,
            boolean featured) {
        return new NewsArticle(
                slug,
                category,
                title,
                summary,
                content,
                imageUrl,
                List.of(imageUrl),
                null,
                featured,
                true,
                Instant.parse(publishedAt));
    }
}
