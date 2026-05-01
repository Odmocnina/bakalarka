package models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***************************************************
 * Annotation for car following model ID, used for identification of car following / lane changing models when starting
 * application, reflectively
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************************************/
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelId {

    /**
     * ID of the car following model, used for identification when starting application
     *
     * @return String of the ID of the car following model/lane changing model
     **/
    String value();
}
