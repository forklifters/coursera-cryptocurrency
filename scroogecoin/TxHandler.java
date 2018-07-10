import java.util.*;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        UTXOPool uPoolCopy = new UTXOPool(utxoPool);
        this.utxoPool = uPoolCopy;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        int totalInput = 0;
        Set<UTXO> utxoSet = new HashSet<>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            // PART 1
            UTXO unverifiedUTXO = new UTXO(input.prevTxHash, input.outputIndex);
            if (!utxoPool.contains(unverifiedUTXO)) {
                return false;
            }
            // PART 2
            Transaction.Output corrOutput = utxoPool.getTxOutput(unverifiedUTXO);
            if (!Crypto.verifySignature(corrOutput.address, tx.getRawDataToSign(i), input.signature)) {
                return false;
            }
            // PART 3
            if (utxoSet.contains(unverifiedUTXO)) {
                return false;
            } else {
                utxoSet.add(unverifiedUTXO);
            }

            totalInput += corrOutput.value;
        }

        int totalOutput = 0;
        for (Transaction.Output output : tx.getOutputs()) {
            // PART 4
            if (output.value < 0) {
                return false;
            }
            totalOutput += output.value;
        }

        // PART 5
        if (totalInput < totalOutput) {
            return false;
        }

        // if everything passes
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> validTxs = new ArrayList<>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);
            }
        }

        // Taking naive (but should be correct) approach to finding mutually valid transaction set by adding in order.
        // Going to keep looping through transactions until no new ones are added because transactions can reference
        //  themselves IN THE SAME BLOCK i.e. in this set of transactions.
        List<Transaction> acceptedTxs = new ArrayList<>();
        List<Transaction> stillValidTxs = new ArrayList<>();
        Set<UTXO> usedUTXOs = new HashSet<>();
        List<UTXO> tempUTXOs;
        boolean shouldAddTx;
        boolean newTxsAdded = true;
        while (newTxsAdded) {
            newTxsAdded = false;
            for (Transaction tx : validTxs) {
                shouldAddTx = true;
                tempUTXOs = new ArrayList<>();
                for (Transaction.Input input : tx.getInputs()) { // validation for a transaction
                    UTXO utxoFromInput = new UTXO(input.prevTxHash, input.outputIndex);
                    if (usedUTXOs.contains(utxoFromInput)) {
                        shouldAddTx = false;
                    }
                    tempUTXOs.add(utxoFromInput);
                }
                if (shouldAddTx) { // after validation
                    acceptedTxs.add(tx);
                    newTxsAdded = true;
                    usedUTXOs.addAll(tempUTXOs);
                } else {
                    stillValidTxs.add(tx); // we want to cycle through transactions again, but not ones that have been used already
                }
            }
            validTxs = stillValidTxs;
            stillValidTxs = new ArrayList<>();
        }

        for (UTXO utxo : usedUTXOs) {
            utxoPool.removeUTXO(utxo);
        }

        for (Transaction tx : acceptedTxs) {
            for (int i = 0; i < tx.numOutputs(); i++) {
                utxoPool.addUTXO(new UTXO(tx.getHash(), i), tx.getOutput(i));
            }
        }

        Transaction[] txs = acceptedTxs.toArray(new Transaction[0]);

        return txs;
    }

}
