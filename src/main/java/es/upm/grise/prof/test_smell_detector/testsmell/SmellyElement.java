package es.upm.grise.prof.test_smell_detector.testsmell;

import java.util.Map;

public abstract class SmellyElement {
    public abstract String getElementName();

    public abstract boolean getHasSmell();

    public abstract Map<String, String> getData();
}
