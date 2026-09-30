package com.webtrans.school.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class RoutePathService {

    private final List<double[]> forwardPath;
    private final List<double[]> returnPath;

    public RoutePathService() {
        List<double[]> loaded = loadExactStreetPath();
        this.forwardPath = Collections.unmodifiableList(loaded);

        List<double[]> rev = new ArrayList<>(loaded);
        Collections.reverse(rev);
        this.returnPath = Collections.unmodifiableList(rev);
    }

    private List<double[]> loadExactStreetPath() {
        List<double[]> pts = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/street_path.json")) {
            if (is != null) {
                List<List<Double>> raw = new ObjectMapper().readValue(is, new TypeReference<>() {});
                for (List<Double> p : raw) {
                    if (p.size() >= 2) {
                        pts.add(new double[]{p.get(0), p.get(1)});
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load street_path.json from resources, using fallback: " + e.getMessage());
        }

        if (pts.isEmpty()) {
            // Fallback keypoints
            double[][] fallback = {
                    {6.840199, 79.965426}, {6.844896, 79.970320}, {6.854895, 79.981137},
                    {6.864940, 79.995866}, {6.879107, 79.986004}, {6.885251, 79.983815},
                    {6.893405, 79.979121}, {6.898477, 79.971471}, {6.907323, 79.973843},
                    {6.914707, 79.972646}
            };
            for (double[] pt : fallback) {
                pts.add(pt);
            }
        }
        return pts;
    }

    public List<double[]> getPathForRoute(Long routeId) {
        if (routeId != null && routeId == 2L) {
            return returnPath;
        }
        return forwardPath;
    }

    public List<double[]> getForwardPath() {
        return forwardPath;
    }

    public List<double[]> getReturnPath() {
        return returnPath;
    }
}