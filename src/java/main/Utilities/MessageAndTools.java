package Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by yifan on 12/19/16.
 * immutable
 */
public class MessageAndTools {
    private final MsgTypes type;
    private final Map<String, Object> fields;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(' ');
        switch (type) {
            case P1A:{

            }
            case P1B:{

            }
            case P2A:{

            }
            case P2B:{

            }
            case DECISION:{

            }
            case PROPOSE:{

            }
            case REQUEST:{

            }
            case RESPONSE:{

            }
        }
        return sb.toString();
    }

    public MessageAndTools(String rawMsg) {
        Scanner sc = new Scanner(rawMsg);
        MsgTypes type = MsgTypes.valueOf(sc.next());
        this.type = type;
        this.fields = new HashMap<>();
        switch (type) {
            case P1A:{

            }
            case P1B:{

            }
            case P2A:{

            }
            case P2B:{

            }
            case DECISION:{

            }
            case PROPOSE:{

            }
            case REQUEST:{

            }
            case RESPONSE:{

            }
        }
    }

    public MessageAndTools(MsgTypes type, Map<String, Object> map) {
        this.type = type;
        //copy map
        this.fields = new HashMap<>(map);
    }

    public MsgTypes getType() {
        return this.type;
    }

    public Object getField(String field) {
        return fields.get(field);
    }
}
