import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class GNFA {
    private final Set<String> states;
    private String startState;
    private String acceptState;
    private final Map<String, Map<String, String>> transitions; // Transitions with regex labels

    // METHOD : CONSTRUCTOR GNFA()
    /* description : 
     * Constructor to initialize states, start and accept states, and create an empty transition map
     */
    public GNFA(Set<String> states, String startState, String acceptState) {
        this.states = states;
        this.startState = startState;
        this.acceptState = acceptState;
        this.transitions = new HashMap<>();
        // Initialize transition maps for each state
        for (String state : states) {
            transitions.put(state, new HashMap<>());
        }
    }


    // METHOD : addTransition()
    /* description : 
    *  Adds a transition with regex from `from` state to `to` state
    If a transition already exists, merges using OR (|)
    */
    public void addTransition(String from, String to, String regex) {
        transitions.get(from).merge(to, regex, (oldVal, newVal) -> "(" + oldVal + "|" + newVal + ")");
    }

    // METHOD : convertToRegex()
    /* description
     * // Converts the GNFA to a single regular expression
     */
    public String convertToRegex() {
        // Create a set of states to remove, excluding the start and accept states
        Set<String> remainingStates = new HashSet<>(states);
        remainingStates.remove(startState);
        remainingStates.remove(acceptState);

        // Iteratively remove each intermediate state to simplify transitions
        for (String state : remainingStates) {
            removeState(state); // Removes one state at a time and adjusts transitions
        }

        // After all intermediate states are removed, return the regex from start to accept state
        return transitions.get(startState).get(acceptState);
    }


    // METHOD : removeState()
    /* dscription : 
     * // Removes a state and adjusts transitions to form equivalent regex between remaining states
     */
    private void removeState(String state) {
        // Check for and handle self-loop by making it (loopRegex)*
        String loopRegex = transitions.getOrDefault(state, new HashMap<>()).getOrDefault(state, "");
        if (!loopRegex.isEmpty()) {
            loopRegex = "(" + loopRegex + ")*"; // Represent zero or more occurrences of the loop
        }
    
        // Update transitions for paths that pass through the removed state
        for (String p : states) {
            // Skip if no transition exists from p to the state being removed
            if (p.equals(state) || !transitions.containsKey(p) || !transitions.get(p).containsKey(state)) continue;
            
            for (String q : states) {
                // Skip if no transition exists from the state to q
                if (q.equals(state) || !transitions.containsKey(state) || !transitions.get(state).containsKey(q)) continue;
    
                // Retrieve existing transitions, handling cases where paths might be null
                String pq = transitions.getOrDefault(p, new HashMap<>()).getOrDefault(q, "");
                String ps = transitions.get(p).getOrDefault(state, ""); // Path from p to state
                String sq = transitions.get(state).getOrDefault(q, ""); // Path from state to q
    
                // New regex for p to q going through state: ps + loopRegex + sq
                String newRegex = ps + loopRegex + sq;
                if (!pq.isEmpty()) newRegex = "(" + pq + "|" + newRegex + ")"; // Merge with existing path using OR
                transitions.get(p).put(q, newRegex); // Update transition from p to q
            }
        }
    
        // Remove all transitions involving the removed state
        for (String s : states) {
            if (transitions.containsKey(s)) {
                transitions.get(s).remove(state); // Remove any incoming paths to the state
            }
        }
        transitions.remove(state); // Remove all outgoing paths from the state
    }
    

    // DRIVER CODE : 
    public static void main(String[] args) 
    {
        // Define the states for the GNFA
        Scanner scan = new Scanner(System.in);
        Set<String> states = new HashSet<>();
        System.out.println("Enter the states of GNFA, type \"END\" when done : ");
        
        while(true){
            String state = scan.nextLine();
            if (state.equals("END")) {
                break;
            }
            states.add(state);
        }
        String startState, finalState;
        System.out.println("Mention the Start State and the Final State : ");
        startState = scan.nextLine();
        finalState = scan.nextLine();

        if(!states.contains(startState) || !states.contains(finalState))
        {
            System.out.println("Start state and final state are not found in the set of states.");
            scan.close();
            return;
        }
        
        // Initialize the GNFA with start and accept states
        GNFA gnfa = new GNFA(states, startState, finalState);
        

        // ACCEPTING TRANSITIONS FROM USER : 
        System.out.println("Enter the transitions (Type \"END\" when done.): ");
        while(true)
        {
            String fromState, toState, regex;
            System.out.println("State from : ");
            fromState = scan.nextLine();
            if(fromState.equals("END"))
                break;

            System.out.println("State to : ");
            toState = scan.nextLine();
            System.out.println("Enter regex : ");
            regex = scan.nextLine();

            if(!states.contains(fromState) || !states.contains(toState))
            {
                System.out.println("\"from\" state and \"to\" state are not found in the set of states.");
                scan.close();
                return;
            }

            // adding the transition to the GNFA
            gnfa.addTransition(fromState, toState, regex);
        }
        
       
        String regex = gnfa.convertToRegex();
        System.out.println("Regular Expression: " + regex);
        scan.close();
    }
}
