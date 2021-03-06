package org.cyanteam.telemaniacs.core.services;

import org.cyanteam.telemaniacs.core.dao.UserDao;
import org.cyanteam.telemaniacs.core.entities.Transmission;
import org.cyanteam.telemaniacs.core.entities.TransmissionOccurrence;
import org.cyanteam.telemaniacs.core.entities.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Simona Tinkova
 */
@Service
public class FavoriteTransmissionsServiceImpl implements FavoriteTransmissionsService {
    @Inject
    private UserDao userDao;

    @Override
    public void followTransmission(Transmission transmission, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        if (transmission == null) {
            throw new IllegalArgumentException("Transmission is null");
        }

        List<Transmission> favoriteTransmissions = user.getFavoriteTransmissions();

        if (!favoriteTransmissions.contains(transmission)) {
            favoriteTransmissions.add(transmission);
        }

        user.setFavoriteTransmissions(favoriteTransmissions);
        userDao.update(user);
    }

    @Override
    public void unfollowTransmission(Transmission transmission, User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        if (transmission == null) {
            throw new IllegalArgumentException("Transmission is null");
        }

        List<Transmission> favoriteTransmissions = user.getFavoriteTransmissions();

        if (favoriteTransmissions.contains(transmission)) {
            favoriteTransmissions.remove(transmission);
        }

        user.setFavoriteTransmissions(favoriteTransmissions);
        userDao.update(user);
    }

    @Override
    public List<Transmission> getFavoriteTransmissionsByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        return user.getFavoriteTransmissions();
    }

    @Override
    public List<Transmission> getUpcomingFavoriteTransmissionsByUser(User user, Duration maxTimeSpan) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }

        if (maxTimeSpan == null) {
            throw new IllegalArgumentException("MaxTimeSpan is null");
        }

        if (user.getFavoriteTransmissions() == null) {
            return new ArrayList<>();
        }

        List<Transmission> transmissions = user.getFavoriteTransmissions();
        transmissions.stream()
            .forEach(t -> t.setOccurrences(filterByTimeSpan(t.getOccurrences(), maxTimeSpan)));
        return transmissions.stream()
            .filter(t -> !t.getOccurrences().isEmpty())
            .sorted((t1, t2) -> t1.getOccurrences().get(0).getStartDate().compareTo(t2.getOccurrences().get(0).getStartDate()))
            .collect(Collectors.toList());
    }

    private List<TransmissionOccurrence> filterByTimeSpan(List<TransmissionOccurrence> src, Duration maxTimeSpan) {
        LocalDateTime now = LocalDateTime.now();
        return src.stream()
            .filter(o -> o.getStartDate().isAfter(now) && o.getStartDate().isBefore(now.plus(maxTimeSpan)))
            .sorted((o1, o2) -> o1.getStartDate().compareTo(o2.getStartDate()))
            .collect(Collectors.toList());
    }
}
