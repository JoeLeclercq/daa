

public class SpecializedMinHeap implements SpecializedMinHeapInterface {
    private final int INITIAL_SIZE = 8; // initial min size of empty heap arrays
    private int numVertices;
    private int[] heapIndices;
    private Data[] minHeap;

    /**
     * Initializes an empty min heap with no vertices.
     */
    public SpecializedMinHeap() {
        this(-1, 0);
    }

    /**
     * Initializes the min heap with a distance of 0 for the source vertex
     * and Integer.MAX_VALUE for the remaining vertices.
     */
    //initializes MinHeap with value (distance) info for source vertex and
    //remaining specified number of vertices; initializes MinHeap location
    //for each vertex
    public SpecializedMinHeap(int source, int numVertices) {
        this.heapIndices = new int[numVertices > INITIAL_SIZE ? numVertices : INITIAL_SIZE];
        this.minHeap = new Data[numVertices > INITIAL_SIZE ? numVertices : INITIAL_SIZE];
        this.numVertices = numVertices;

        if (numVertices > 0) {
            // Initial location for all vertices before source is that vertex + 1
            // to account for the shift caused by putting the source at the head of
            // the max heap.
            for (int i = 0; i < source; i++) {
                heapIndices[i] = i + 1;
                minHeap[i + 1] = new Data(i, Integer.MAX_VALUE);
            }

            // Put source at top of the max heap with distance 0.
            heapIndices[source] = 0;
            minHeap[0] = new Data(source, 0);

            // All vertices after the source are in the position corresponding to
            // the index equalling that vertex, but still infinite distance.
            for (int i = source; i < numVertices; i++) {
                heapIndices[i] = i;
                minHeap[i] = new Data(i, Integer.MAX_VALUE);
            }

            for (int i = numVertices; i < heapIndices.length; i++) {
                heapIndices[i] = -1;
            }
        }
    }

    /**
     * @return true if the min heap is empty, false otherwise.
     */
    public boolean isEmpty() {
        return numVertices == 0;
    }

    /**
     * Inserts a new vertex into the min heap with a given value.
     *
     * @return the outcome of the insertion
     */
    public boolean insert(int vertex, int value) {
        // Resize arrays if too small
        if (numVertices == minHeap.length) {
            Data[] newMinHeap = new Data[numVertices * 2];
            int[] newHeapIndices = new int[numVertices * 2];

            // copy old info
            for (int i = 0; i < numVertices * 2; i++) {
                newMinHeap[i] = minHeap[i];
                newHeapIndices[i] = heapIndices[i];
            }

            this.minHeap = newMinHeap;
            this.heapIndices = newHeapIndices;
        }

        int currentIndex = numVertices;
        Data newNode = new Data(vertex, value);
        // insert in leaf position
        this.heapIndices[vertex] = currentIndex;
        this.minHeap[currentIndex] = newNode;

        // no shifts needed, exit immediately
        if (numVertices == 0) {
            return true;
        }

        heapFilterUp(currentIndex);

        this.numVertices++;
        return true;
    }

    /**
     * Does not check for good input. "Bubbles up" the heap into the correct position
     * so that the array becomes heapified starting at the specified index.
     * @param index Index of the node in the heap array to filter up.
     */
    private void heapFilterUp(int index) {
        Data node = minHeap[index];
        // bubble up to position satisfying min heap property
        int parentIndex = (index + 1) / 2;
        Data parent = minHeap[parentIndex];

        // stop when it's at the top or min heap satisfied
        while (parentIndex != index || parent.getValue() > node.getValue()) {
            // swap parent and new node
            minHeap[parentIndex] = node;
            minHeap[index] = parent;
            heapIndices[parent.getVertex()] = index;
            heapIndices[node.getVertex()] = parentIndex;

            // set new current index and update parent info
            index = parentIndex;
            parentIndex = (index + 1) / 2;
            parent = minHeap[parentIndex];
        }
    }

    /**
     * Does not check for good input. "Trickles down" the heap into the correct position
     * so that the array becomes heapified starting at the specified index.
     * @param index Index of the node in the heap array to filter up.
     */
    private void heapFilterDown(int index) {
        // index is used as the working index.
        Data node = minHeap[index];
        boolean heapified;

        do {
            heapified = true;
            int smallerChildIndex = index;
            int smallestVal = minHeap[smallerChildIndex].getValue();
            int leftIndex = 2 * index + 1;
            int rightIndex = 2 * index + 2;

            if (leftIndex < numVertices) {
                if (minHeap[leftIndex].getValue() < smallestVal) {
                    smallerChildIndex = leftIndex;
                    smallestVal = smallerChildIndex;
                }

                if (rightIndex < numVertices && minHeap[rightIndex].getValue() < smallestVal) {
                    smallerChildIndex = rightIndex;
                }

                // There is a swap to be done
                if (smallerChildIndex != index) {
                    heapified = false;

                    // Place child into parent spot
                    minHeap[index] = minHeap[heapIndices[smallerChildIndex]];
                    // Place parent into child spot
                    minHeap[smallerChildIndex] = node;

                    heapIndices[smallerChildIndex] = index;
                    heapIndices[index] = smallerChildIndex;
                }
            }
        } while (!heapified);
    }

    private Data deleteNode(int index) {
        if (index >= numVertices || index < 0) {
            return null;
        }

        // Delete node. Insert what's in the right most bottom index into the deleted index. heapify.
        Data toDelete = minHeap[index];
        int rightMostIndex = numVertices - 1;
        Data rightMost = minHeap[rightMostIndex];
        int parentIndex = (index + 1) / 2;
        numVertices--;

        minHeap[index] = rightMost;
        minHeap[rightMostIndex] = null;
        heapIndices[rightMost.getVertex()] = index;
        heapIndices[toDelete.getVertex()] = -1;

        // If the new replacement node is smaller than its new parent, bubble up
        // otherwise trickle down
        if (rightMost.getValue() < minHeap[parentIndex].getValue()) {
            heapFilterUp(index);
        } else {
            heapFilterDown(index);
        }

        return toDelete;
    }

    private Data deleteVertex(int vertex) {
        if (vertex < 0 || vertex > heapIndices.length) {
            return null;
        }

        return deleteNode(heapIndices[vertex]);
    }

    /**
     * Deletes the vertex with the smallest value from the min heap.
     * 
     * @return the wrapper for the vertex deleted
     */
    public Data deleteMin() {
        return deleteNode(0);
    }

    /**
     * Attempts, if possible, to decrease the value of the given vertex
     * to a new value. Moves the vertex to the correct new position in
     * the heap. 
     * @return true if the vertex decreased in value and was moved,
     * false otherwise
     */
    public boolean decreaseKey(int vertex, int newValue) {
        if (vertex >= heapIndices.length || heapIndices[vertex] == -1) {
            return false;
        }

        deleteNode(heapIndices[vertex]);
        insert(vertex, newValue);

        return true;
    }

    public void printInfo() {
        System.out.print("\t");
        for (int i = 0; i < heapIndices.length; i++) {
            System.out.printf("%d\t", i);
        }
        System.out.println();
        System.out.print("HI:\n");
        for (int heapIndex : heapIndices) {
            System.out.printf("%d\t", heapIndex);
        }
        System.out.println();
        System.out.print("HVal:\t");
        for (int i = 0; i < heapIndices.length; i++) {
            System.out.printf("%d\t", minHeap[i].getValue());
        }
        System.out.println();
        System.out.print("HVert:\t");
        for (int i = 0; i < heapIndices.length; i++) {
            System.out.printf("%d\t", minHeap[i].getVertex());
        }
        System.out.println();
    }
}
