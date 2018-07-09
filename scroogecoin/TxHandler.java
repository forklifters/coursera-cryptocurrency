import java.util.HashSet;
import java.util.Set;

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
        Transaction[] txs = {};
        return txs;
    }

}
