package in.einfosolutions.koble.utilities;

import com.google.gson.*;
import org.joda.time.DateTime;
import java.lang.reflect.Type;

public class DateTimeDeserializer implements JsonDeserializer<DateTime> {

    @Override
    public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new DateTime(json.getAsJsonPrimitive().getAsString());
    }
}
