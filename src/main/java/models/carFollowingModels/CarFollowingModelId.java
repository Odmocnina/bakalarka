package models.carFollowingModels;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***************************************************
 * Annotation for car following model ID, used for identification when starting application, reflectively
 *
 * @author Michael Hladky
 * @version 1.0
 ***************************************************/
@Retention(RetentionPolicy.RUNTIME)
public @interface CarFollowingModelId {

    /**
     * ID of the car following model, used for identification when starting application
     */
    String value();
}
