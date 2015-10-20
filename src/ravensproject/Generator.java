package ravensproject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a generator class for all helper functions that need to
 * be generic.
 */
public class Generator<E> {

    public List<List<E>> generatePermutations(List<E> original) {
        if (original.size() == 0) {
            List<List<E>> result = new ArrayList<>();
            result.add(new ArrayList<E>());
            return result;
        }
        E firstElement = original.remove(0);
        List<List<E>> permutationList = new ArrayList<>();
        List<List<E>> permutations = generatePermutations(original);
        for (List<E> smaller : permutations) {
            for (int index=0; index <= smaller.size(); index++) {
                List<E> temp = new ArrayList<>(smaller);
                temp.add(index, firstElement);
                permutationList.add(temp);
            }
        }
        return permutationList;
    }

    /**
     * This method forms pairs between elements of two arrays and returns a list of such
     * of said pairs.
     *
     * @param list1
     * @param list2
     * @return The list of pairs
     */
    public List<List<E>> formPairs(List<E> list1, List<E> list2) {

        // Todo - figure out better way to make sure everything paired
        // i.e. B-12 --> If one list bigger than other, certain pairs
        // are not formed that could be useful

        List<List<E>> pairList = new ArrayList<>();

        List<E> tempList = list1;
        if (list2.size() < list1.size())
            tempList = list2;

        for (int i = 0; i < tempList.size(); i++) {
            List<E> pair = new ArrayList<>();
            pair.add(list1.get(i));
            pair.add(list2.get(i));
            pairList.add(pair);
        }

        return pairList;
    }

    public List<E> intersection(List<E> list1, List<E> list2) {
        List<E> list = new ArrayList<>();

        for (E element : list1) {
            if(list2.contains(element)) {
                list.add(element);
            }
        }

        return list;
    }
}
