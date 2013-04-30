Description
-
BayesBox is a pure Java library the allows you to create/import/manipulate and execute computations on **bayesian networks**.

Methods
--
Computations can be made with both exact (**Enumeration**) and statistically-simulated methods (**Likelihood weighting**, **Rejection sampling**)

Network creation
--
-Via code:
<pre><code>//...
BayesNetNode root1 = new BayesNetNode("event 1");
BayesNetNode root2 = new BayesNetNode("event 2");
BayesNetNode child = new BayesNetNode("child");

root1.setProbability(true, 0.4);
root2.setProbability(true, 0.8);
child.influencedBy(root1,root2);
child.setProbability(0.3, true,true);
child.setProbability(0.4, true,false);
child.setProbability(0.5, false,true);
child.setProbability(0.6, false,false);

BayesNet bn = new BayesNet(root1,root2);
//...
</code></pre>
-Using **BayesBuilder**, exporting your net in XML format 
(freely available at http://www.snn.ru.nl/nijmegen/index.php?option=com_content&view=article&id=89&Itemid=212) 

Usage example:
--
<pre><code>//
BayesBox box = new BayesBox("example_net.xml"); //Created with BayesBuilder
BayesNet bn = box.getBayesNet();

Hashtable evidence = new Hashtable();
evidence.put("event 1", true);
evidence.put("event 2", true);

double ris[] = bn.enumerationAsk("child", evidence, BayesNet.MODE_DESCRIPTION)
double ris2[] = bn.rejectionSample(var, evidence, 5000, BayesNet.MODE_DESCRIPTION);
//...

</code></pre>

